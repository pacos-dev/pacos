package org.pacos.core.component.notification.dto;

import java.time.Instant;

import org.pacos.base.notification.NotificationCategory;
import org.pacos.base.notification.NotificationPriority;
import org.pacos.core.component.notification.domain.NotificationItemType;
import org.pacos.core.component.notification.domain.NotificationOperationStatus;

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
