package org.pacos.core.component.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.base.notification.NotificationAudience;
import org.pacos.base.notification.NotificationCategory;
import org.pacos.base.notification.NotificationPriority;
import org.pacos.base.notification.NotificationPublishResult;
import org.pacos.base.notification.NotificationRequest;
import org.pacos.core.component.notification.domain.NotificationItemType;
import org.pacos.core.component.notification.domain.NotificationMessage;
import org.pacos.core.component.notification.domain.NotificationRecipient;
import org.pacos.core.component.notification.domain.NotificationUserState;
import org.pacos.core.component.notification.repository.NotificationMessageRepository;
import org.pacos.core.component.notification.repository.NotificationOperationRepository;
import org.pacos.core.component.notification.repository.NotificationRecipientRepository;
import org.pacos.core.component.notification.repository.NotificationUserStateRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationMessageRepository messageRepository;

    @Mock
    private NotificationOperationRepository operationRepository;

    @Mock
    private NotificationRecipientRepository recipientRepository;

    @Mock
    private NotificationUserStateRepository userStateRepository;

    @InjectMocks
    private NotificationService service;

    @Test
    void republishingSameBusinessKeyReusesMessageAndPreservesReadState() {
        Instant createdAt = Instant.parse("2026-10-01T10:00:00Z");
        NotificationMessage existing = new NotificationMessage();
        existing.setId(41L);
        existing.setTitle("Calendar plugin");
        existing.setSourceKey("APP_CENTER");
        existing.setBusinessKey("plugin:org.example:calendar:capabilities");
        existing.setSummary("A calendar plugin");
        existing.setCategory(NotificationCategory.ANNOUNCEMENT);
        existing.setPriority(NotificationPriority.INFO);
        existing.setAudience(NotificationAudience.ALL_USERS);
        existing.setCreatedAt(createdAt);
        existing.setActive(true);
        when(messageRepository.findBySourceKeyAndBusinessKey(
                "APP_CENTER", "plugin:org.example:calendar:capabilities")).thenReturn(Optional.of(existing));
        when(recipientRepository.findByNotificationId(41L)).thenReturn(List.of());

        NotificationRequest request = new NotificationRequest(
                "APP_CENTER", "plugin:org.example:calendar:capabilities", "Calendar plugin",
                "A calendar plugin", null, NotificationCategory.ANNOUNCEMENT, NotificationPriority.INFO,
                null, null, false, NotificationAudience.ALL_USERS, Set.of());

        NotificationPublishResult result = service.publish(request);

        assertEquals(41L, result.notificationId());
        assertEquals(NotificationPublishResult.Outcome.UNCHANGED, result.outcome());
        assertEquals(createdAt, existing.getCreatedAt());
        verify(messageRepository, never()).save(any(NotificationMessage.class));
        verify(recipientRepository, never()).deleteByNotificationId(41L);
    }

    @Test
    void readStateIsSavedIndependentlyForEachUser() {
        when(messageRepository.existsById(41L)).thenReturn(true);
        when(userStateRepository.findByUserIdAndItemTypeAndItemId(anyInt(), any(), anyLong()))
                .thenReturn(Optional.empty());
        when(userStateRepository.save(any(NotificationUserState.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.markRead(10, NotificationItemType.MESSAGE, 41L);
        service.markRead(11, NotificationItemType.MESSAGE, 41L);

        ArgumentCaptor<NotificationUserState> captor = ArgumentCaptor.forClass(NotificationUserState.class);
        verify(userStateRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertEquals(10, captor.getAllValues().get(0).getUserId());
        assertEquals(11, captor.getAllValues().get(1).getUserId());
        assertEquals(NotificationItemType.MESSAGE, captor.getAllValues().get(0).getItemType());
        assertEquals(41L, captor.getAllValues().get(0).getItemId());
        assertEquals(41L, captor.getAllValues().get(1).getItemId());
    }

    @Test
    void publishWithoutBusinessKeyAlwaysCreatesANewMessage() {
        NotificationRequest request = new NotificationRequest(
                "APP_CENTER", null, "Calendar plugin", null, null,
                NotificationCategory.ANNOUNCEMENT, NotificationPriority.INFO, null, null,
                false, NotificationAudience.ALL_USERS, List.of());
        when(messageRepository.save(any(NotificationMessage.class))).thenAnswer(invocation -> {
            NotificationMessage message = invocation.getArgument(0);
            message.setId(73L);
            return message;
        });
        when(recipientRepository.findByNotificationId(73L)).thenReturn(List.of());

        NotificationPublishResult result = service.publish(request);

        assertEquals(73L, result.notificationId());
        assertEquals(NotificationPublishResult.Outcome.CREATED, result.outcome());
        verify(messageRepository).save(any(NotificationMessage.class));
    }
}
