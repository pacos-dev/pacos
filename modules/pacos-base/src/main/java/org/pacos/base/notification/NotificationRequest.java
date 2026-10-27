package org.pacos.base.notification;

import java.time.Instant;
import java.util.Set;

/**
 * Immutable request to publish a message to the PacOS Notification Center.
 *
 * The optional business key identifies one logical announcement. Reusing the same
 * source key and business key updates that announcement instead of creating a duplicate.
 */
public record NotificationRequest(
        String sourceKey,
        String businessKey,
        String title,
        String summary,
        String body,
        NotificationCategory category,
        NotificationPriority priority,
        Instant expiresAt,
        String actionUrl,
        boolean requiresAcknowledgement,
        NotificationAudience audience,
        Set<Integer> recipientUserIds) {

    public NotificationRequest {
        sourceKey = requireText(sourceKey, "sourceKey").toUpperCase(java.util.Locale.ROOT);
        title = requireText(title, "title");
        if (businessKey != null) {
            businessKey = requireText(businessKey, "businessKey");
            requireMaxLength(businessKey, 255, "businessKey");
        }
        requireMaxLength(sourceKey, 100, "sourceKey");
        requireMaxLength(title, 255, "title");
        requireMaxLength(summary, 1000, "summary");
        requireMaxLength(body, 12000, "body");
        requireMaxLength(actionUrl, 2048, "actionUrl");
        category = category == null ? NotificationCategory.SYSTEM : category;
        priority = priority == null ? NotificationPriority.INFO : priority;
        audience = audience == null ? NotificationAudience.ALL_USERS : audience;
        if (recipientUserIds != null && recipientUserIds.stream().anyMatch(userId -> userId == null || userId <= 0)) {
            throw new IllegalArgumentException("Recipient user IDs must be positive");
        }
        recipientUserIds = recipientUserIds == null ? Set.of() : Set.copyOf(recipientUserIds);

        if (audience == NotificationAudience.ALL_USERS && !recipientUserIds.isEmpty()) {
            throw new IllegalArgumentException("ALL_USERS notifications must not specify recipient IDs");
        }
        if (audience == NotificationAudience.SELECTED_USERS && recipientUserIds.isEmpty()) {
            throw new IllegalArgumentException("SELECTED_USERS notifications require at least one recipient");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static void requireMaxLength(String value, int maximum, String fieldName) {
        if (value != null && value.length() > maximum) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + maximum + " characters");
        }
    }
}
