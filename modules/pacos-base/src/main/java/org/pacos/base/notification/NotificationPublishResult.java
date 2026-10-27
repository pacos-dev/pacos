package org.pacos.base.notification;

/**
 * Result of publishing a notification.
 */
public record NotificationPublishResult(long notificationId, Outcome outcome) {

    public enum Outcome {
        CREATED,
        UPDATED,
        UNCHANGED
    }
}
