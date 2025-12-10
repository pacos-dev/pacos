package org.pacos.base.utils.notification;

/**
 * Message type for {@link NotificationUtils}
 */
public enum MessageType {
    INFO, ERROR, SUCCESS;

    public boolean isError() {
        return this == ERROR;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
