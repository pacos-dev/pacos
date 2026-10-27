package org.pacos.core.component.notification.service;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.pacos.base.session.UserSession;
import org.pacos.core.component.notification.domain.NotificationCategory;
import org.pacos.core.component.notification.domain.NotificationItemType;
import org.pacos.core.component.notification.domain.NotificationMessage;
import org.pacos.core.component.notification.domain.NotificationOperation;
import org.pacos.core.component.notification.domain.NotificationPriority;
import org.pacos.core.component.notification.domain.NotificationUserState;
import org.pacos.core.component.notification.dto.NotificationCenterItemDTO;
import org.pacos.core.component.notification.repository.NotificationMessageRepository;
import org.pacos.core.component.notification.repository.NotificationOperationRepository;
import org.pacos.core.component.notification.repository.NotificationUserStateRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.vaadin.flow.server.VaadinSession;

@Service
public class NotificationService {

    private static final String GUEST_READ_STATE_KEY = "notification-center.read-state";
    private static final String GUEST_ACK_STATE_PREFIX = "ACK:";
    private static final int MAX_QUERY_SIZE = 500;

    private final NotificationMessageRepository messageRepository;
    private final NotificationOperationRepository operationRepository;
    private final NotificationUserStateRepository userStateRepository;

    public NotificationService(NotificationMessageRepository messageRepository,
                               NotificationOperationRepository operationRepository,
                               NotificationUserStateRepository userStateRepository) {
        this.messageRepository = messageRepository;
        this.operationRepository = operationRepository;
        this.userStateRepository = userStateRepository;
    }

    @Transactional("coreTransactionManager")
    public NotificationMessage publish(NotificationMessage message) {
        Objects.requireNonNull(message, "message");
        if (!StringUtils.hasText(message.getTitle())) {
            throw new IllegalArgumentException("Notification title must not be empty");
        }
        if (!StringUtils.hasText(message.getSourceKey())) {
            throw new IllegalArgumentException("Notification source key must not be empty");
        }
        if (message.getCategory() == null) {
            message.setCategory(NotificationCategory.SYSTEM);
        }
        if (message.getPriority() == null) {
            message.setPriority(NotificationPriority.INFO);
        }
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(Instant.now());
        }
        message.setActive(true);
        return messageRepository.save(message);
    }

    @Transactional(readOnly = true, transactionManager = "coreTransactionManager")
    public List<NotificationCenterItemDTO> findRecentItems(int userId, int requestedLimit,
                                                             String sourceKey,
                                                             NotificationCategory category,
                                                             NotificationPriority priority,
                                                             boolean unreadOnly) {
        int limit = Math.max(1, Math.min(requestedLimit, MAX_QUERY_SIZE));
        Instant now = Instant.now();
        List<NotificationMessage> messages = messageRepository
                .findByActiveTrueOrderByCreatedAtDesc(PageRequest.of(0, limit));
        List<NotificationOperation> operations = operationRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));

        Set<Long> messageIds = messages.stream().map(NotificationMessage::getId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> operationIds = operations.stream().map(NotificationOperation::getId)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, NotificationUserState> messageStates = loadStates(userId, NotificationItemType.MESSAGE, messageIds);
        Map<Long, NotificationUserState> operationStates = loadStates(userId, NotificationItemType.OPERATION, operationIds);
        Set<String> guestReadKeys = getGuestReadKeys(userId);

        Stream<NotificationCenterItemDTO> messageItems = messages.stream()
                .filter(message -> message.getExpiresAt() == null || message.getExpiresAt().isAfter(now))
                .map(message -> toMessageItem(message, messageStates.get(message.getId()), guestReadKeys));
        Stream<NotificationCenterItemDTO> operationItems = operations.stream()
                .map(operation -> toOperationItem(operation, operationStates.get(operation.getId()), guestReadKeys));

        return Stream.concat(messageItems, operationItems)
                .filter(item -> !StringUtils.hasText(sourceKey) || sourceKey.equals(item.sourceKey()))
                .filter(item -> category == null || category == item.category())
                .filter(item -> priority == null || priority == item.priority())
                .filter(item -> !unreadOnly || !item.read())
                .sorted(Comparator.comparing(NotificationCenterItemDTO::createdAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true, transactionManager = "coreTransactionManager")
    public int getUnreadCount(int userId) {
        return (int) findRecentItems(userId, MAX_QUERY_SIZE, null, null, null, true).size();
    }

    @Transactional("coreTransactionManager")
    public void markRead(int userId, NotificationItemType itemType, Long itemId) {
        updateReadState(userId, itemType, itemId, true);
    }

    @Transactional("coreTransactionManager")
    public void markUnread(int userId, NotificationItemType itemType, Long itemId) {
        updateReadState(userId, itemType, itemId, false);
    }

    @Transactional("coreTransactionManager")
    public void acknowledge(int userId, Long messageId) {
        NotificationMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Notification does not exist"));
        if (!message.isRequiresAcknowledgement()) {
            throw new IllegalStateException("This notification does not require acknowledgement");
        }
        if (isGuestSession(userId)) {
            Set<String> guestKeys = getGuestReadKeys(userId);
            guestKeys.add(key(NotificationItemType.MESSAGE, messageId));
            guestKeys.add(acknowledgementKey(messageId));
            return;
        }
        NotificationUserState state = userStateRepository
                .findByUserIdAndItemTypeAndItemId(userId, NotificationItemType.MESSAGE, messageId)
                .orElseGet(() -> new NotificationUserState(userId, NotificationItemType.MESSAGE, messageId));
        Instant now = Instant.now();
        if (state.getReadAt() == null) {
            state.setReadAt(now);
        }
        state.setAcknowledgedAt(now);
        userStateRepository.save(state);
    }

    private void updateReadState(int userId, NotificationItemType itemType, Long itemId, boolean read) {
        Objects.requireNonNull(itemType, "itemType");
        Objects.requireNonNull(itemId, "itemId");
        boolean exists = itemType == NotificationItemType.MESSAGE
                ? messageRepository.existsById(itemId) : operationRepository.existsById(itemId);
        if (!exists) {
            throw new IllegalArgumentException("Notification Center item does not exist");
        }
        if (isGuestSession(userId)) {
            markGuestRead(itemType, itemId, read);
            return;
        }
        if (!read) {
            userStateRepository.findByUserIdAndItemTypeAndItemId(userId, itemType, itemId)
                    .filter(state -> state.getAcknowledgedAt() != null)
                    .ifPresentOrElse(state -> {
                        state.setReadAt(null);
                        userStateRepository.save(state);
                    }, () -> userStateRepository.deleteByUserIdAndItemTypeAndItemId(userId, itemType, itemId));
            return;
        }
        NotificationUserState state = userStateRepository
                .findByUserIdAndItemTypeAndItemId(userId, itemType, itemId)
                .orElseGet(() -> new NotificationUserState(userId, itemType, itemId));
        if (state.getReadAt() == null) {
            state.setReadAt(Instant.now());
        }
        userStateRepository.save(state);
    }

    private Map<Long, NotificationUserState> loadStates(int userId, NotificationItemType itemType,
                                                         Collection<Long> ids) {
        if (ids.isEmpty() || isGuestSession(userId)) {
            return Map.of();
        }
        return userStateRepository.findAllByUserIdAndItemTypeAndItemIdIn(userId, itemType, ids)
                .stream().collect(Collectors.toMap(NotificationUserState::getItemId, Function.identity()));
    }

    private NotificationCenterItemDTO toMessageItem(NotificationMessage message, NotificationUserState state,
                                                       Set<String> guestReadKeys) {
        boolean read = state != null && state.getReadAt() != null;
        boolean acknowledged = state != null && state.getAcknowledgedAt() != null;
        if (guestReadKeys.contains(key(NotificationItemType.MESSAGE, message.getId()))) {
            read = true;
        }
        if (guestReadKeys.contains(acknowledgementKey(message.getId()))) {
            acknowledged = true;
        }
        return new NotificationCenterItemDTO(message.getId(), NotificationItemType.MESSAGE,
                message.getTitle(), message.getSummary(), message.getBody(), message.getSourceKey(),
                message.getCategory(), message.getPriority(), message.getCreatedAt(), message.getExpiresAt(),
                read, message.isRequiresAcknowledgement(), acknowledged, message.getActionUrl(),
                null, null, null);
    }

    private NotificationCenterItemDTO toOperationItem(NotificationOperation operation, NotificationUserState state,
                                                         Set<String> guestReadKeys) {
        boolean read = state != null && state.getReadAt() != null;
        if (guestReadKeys.contains(key(NotificationItemType.OPERATION, operation.getId()))) {
            read = true;
        }
        return new NotificationCenterItemDTO(operation.getId(), NotificationItemType.OPERATION,
                operation.getTitle(), operation.getSummary(), operation.getDetails(), operation.getSourceKey(),
                NotificationCategory.OPERATION, operation.getPriority(), operation.getCreatedAt(), null,
                read, false, false, null, operation.getStatus(), operation.getProgress(), operation.getCurrentStep());
    }

    private boolean isGuestSession(int userId) {
        if (VaadinSession.getCurrent() == null) {
            return false;
        }
        UserSession current = VaadinSession.getCurrent().getAttribute(UserSession.class);
        return current != null && current.getUserId() == userId && current.getUser().isGuestSession();
    }

    @SuppressWarnings("unchecked")
    private Set<String> getGuestReadKeys(int userId) {
        if (!isGuestSession(userId)) {
            return Set.of();
        }
        UserSession session = VaadinSession.getCurrent().getAttribute(UserSession.class);
        Object existing = session.getFromSession(GUEST_READ_STATE_KEY);
        if (existing instanceof Set<?> set) {
            return (Set<String>) set;
        }
        Set<String> keys = new HashSet<>();
        session.addToSession(GUEST_READ_STATE_KEY, keys);
        return keys;
    }

    private void markGuestRead(NotificationItemType type, Long id, boolean read) {
        Set<String> keys = getGuestReadKeys(UserSession.getCurrent().getUserId());
        String itemKey = key(type, id);
        if (read) {
            keys.add(itemKey);
        } else {
            keys.remove(itemKey);
        }
    }

    private String acknowledgementKey(Long id) {
        return GUEST_ACK_STATE_PREFIX + key(NotificationItemType.MESSAGE, id);
    }

    private String key(NotificationItemType type, Long id) {
        return type.name() + ":" + id;
    }
}
