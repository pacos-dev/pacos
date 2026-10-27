package org.pacos.core.component.notification.domain;

public enum NotificationOperationStatus {
    PENDING, RUNNING, SUCCEEDED, FAILED, CANCELLED;

    public boolean isTerminal() {
        return this == SUCCEEDED || this == FAILED || this == CANCELLED;
    }
}
