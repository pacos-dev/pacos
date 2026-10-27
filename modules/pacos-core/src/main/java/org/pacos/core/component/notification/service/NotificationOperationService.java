package org.pacos.core.component.notification.service;

import java.time.Instant;
import java.util.List;

import org.pacos.base.notification.NotificationPriority;
import org.pacos.core.component.notification.domain.NotificationOperation;
import org.pacos.core.component.notification.domain.NotificationOperationEvent;
import org.pacos.core.component.notification.domain.NotificationOperationStatus;
import org.pacos.core.component.notification.repository.NotificationOperationEventRepository;
import org.pacos.core.component.notification.repository.NotificationOperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class NotificationOperationService {

    private final NotificationOperationRepository operationRepository;
    private final NotificationOperationEventRepository eventRepository;

    public NotificationOperationService(NotificationOperationRepository operationRepository,
                                        NotificationOperationEventRepository eventRepository) {
        this.operationRepository = operationRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional("coreTransactionManager")
    public NotificationOperation create(String title, String summary, String sourceKey, String details) {
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Operation title must not be empty");
        }
        if (!StringUtils.hasText(sourceKey)) {
            throw new IllegalArgumentException("Operation source key must not be empty");
        }
        NotificationOperation operation = new NotificationOperation();
        operation.setTitle(title);
        operation.setSummary(summary);
        operation.setSourceKey(sourceKey);
        operation.setDetails(details);
        operation.setStatus(NotificationOperationStatus.PENDING);
        operation.setPriority(NotificationPriority.INFO);
        operation.setProgress(0);
        operation.setCreatedAt(Instant.now());
        operation.setUpdatedAt(Instant.now());
        NotificationOperation saved = operationRepository.save(operation);
        appendEvent(saved, "CREATED", StringUtils.hasText(summary) ? summary : title);
        return saved;
    }

    @Transactional("coreTransactionManager")
    public NotificationOperation updateProgress(Long operationId, int progress, String currentStep, String details) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Operation progress must be between 0 and 100");
        }
        NotificationOperation operation = getRequired(operationId);
        requireNonTerminal(operation);
        boolean startedNow = operation.getStatus() == NotificationOperationStatus.PENDING;
        String previousStep = operation.getCurrentStep();
        operation.setStatus(NotificationOperationStatus.RUNNING);
        if (operation.getStartedAt() == null) {
            operation.setStartedAt(Instant.now());
        }
        operation.setProgress(progress);
        operation.setCurrentStep(currentStep);
        if (StringUtils.hasText(details)) {
            operation.setDetails(details);
        }
        NotificationOperation saved = operationRepository.save(operation);
        if (startedNow) {
            appendEvent(saved, "STARTED", StringUtils.hasText(currentStep) ? currentStep : "Operation started");
        } else if (StringUtils.hasText(currentStep) && !currentStep.equals(previousStep)) {
            appendEvent(saved, "STEP_CHANGED", currentStep);
        }
        return saved;
    }

    @Transactional("coreTransactionManager")
    public NotificationOperation complete(Long operationId, String summary) {
        NotificationOperation operation = getRequired(operationId);
        requireNonTerminal(operation);
        operation.setStatus(NotificationOperationStatus.SUCCEEDED);
        operation.setProgress(100);
        operation.setCurrentStep("Completed");
        operation.setFinishedAt(Instant.now());
        if (StringUtils.hasText(summary)) {
            operation.setSummary(summary);
        }
        NotificationOperation saved = operationRepository.save(operation);
        appendEvent(saved, "SUCCEEDED", StringUtils.hasText(summary) ? summary : "Operation completed");
        return saved;
    }

    @Transactional("coreTransactionManager")
    public NotificationOperation fail(Long operationId, String errorMessage) {
        NotificationOperation operation = getRequired(operationId);
        requireNonTerminal(operation);
        operation.setStatus(NotificationOperationStatus.FAILED);
        operation.setPriority(NotificationPriority.ERROR);
        operation.setCurrentStep("Failed");
        operation.setFinishedAt(Instant.now());
        if (StringUtils.hasText(errorMessage)) {
            operation.setDetails(errorMessage);
            operation.setSummary(errorMessage);
        }
        NotificationOperation saved = operationRepository.save(operation);
        appendEvent(saved, "FAILED", StringUtils.hasText(errorMessage) ? errorMessage : "Operation failed");
        return saved;
    }

    @Transactional("coreTransactionManager")
    public NotificationOperation cancel(Long operationId, String reason) {
        NotificationOperation operation = getRequired(operationId);
        requireNonTerminal(operation);
        operation.setStatus(NotificationOperationStatus.CANCELLED);
        operation.setCurrentStep("Cancelled");
        operation.setFinishedAt(Instant.now());
        NotificationOperation saved = operationRepository.save(operation);
        appendEvent(saved, "CANCELLED", StringUtils.hasText(reason) ? reason : "Operation cancelled");
        return saved;
    }

    @Transactional(readOnly = true, transactionManager = "coreTransactionManager")
    public List<NotificationOperationEvent> getHistory(Long operationId) {
        return eventRepository.findByOperation_IdOrderByCreatedAtAsc(operationId);
    }

    private NotificationOperation getRequired(Long operationId) {
        return operationRepository.findById(operationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification operation does not exist"));
    }

    private void requireNonTerminal(NotificationOperation operation) {
        if (operation.getStatus() == null || operation.getStatus().isTerminal()) {
            throw new IllegalStateException("A completed operation cannot be changed");
        }
    }

    private void appendEvent(NotificationOperation operation, String type, String message) {
        eventRepository.save(new NotificationOperationEvent(operation, type, message));
    }
}
