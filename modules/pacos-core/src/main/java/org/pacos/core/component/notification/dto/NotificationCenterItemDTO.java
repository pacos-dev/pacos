package org.pacos.core.component.notification.dto;

import java.time.Instant;

import org.pacos.core.component.notification.domain.NotificationCategory;
import org.pacos.core.component.notification.domain.NotificationItemType;
import org.pacos.core.component.notification.domain.NotificationOperationStatus;
import org.pacos.core.component.notification.domain.NotificationPriority;

public record NotificationCenterItemDTO(
        Long id,
        NotificationItemType itemType,
        String title,
        String summary,
        String body,
        String sourceKey,
        NotificationCategory category,
        NotificationPriority priority,
        Instant createdAt,
        Instant expiresAt,
        boolean read,
        boolean requiresAcknowledgement,
        boolean acknowledged,
        String actionUrl,
        NotificationOperationStatus operationStatus,
        Integer progress,
        String currentStep) {
}
