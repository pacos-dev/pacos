package org.pacos.base.utils.notification;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationUtils extends Notification {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationUtils.class);

    private NotificationUtils() {
    }

    public static Notification show(String text) {
        return show(text, MessageType.INFO);
    }

    public static Notification show(String text, MessageType type) {
        Notification not = new Notification(text, 5000);
        if (type.isError()) {
            not.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
        if (type.isSuccess()) {
            not.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
        displayNotification(not);
        return not;
    }

    public static Notification error(String text) {
        Notification not = new Notification(text, 5000);
        not.addThemeVariants(NotificationVariant.LUMO_ERROR);
        displayNotification(not);
        return not;
    }

    public static Notification success(String text) {
        Notification not = new Notification(text, 5000);
        not.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        displayNotification(not);
        return not;
    }

    public static Notification info(String text) {
        Notification not = new Notification(text, 5000);
        displayNotification(not);
        return not;
    }

    public static Notification success(String text, Position position) {
        Notification not = new Notification(text, 5000);
        not.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        not.setPosition(position);
        displayNotification(not);
        return not;
    }

    public static Notification success(String text, Object... args) {
        return success(String.format(text, args));
    }

    public static void error(Exception e) {
        LOG.error("Error occurred: ", e);
        if (e.getMessage() != null) {
            error(e.getMessage());
        }
    }

    private static void displayNotification(Notification not) {
        not.setPosition(Position.BOTTOM_END);
        if (UI.getCurrent() != null) {
            not.open();
        }
    }
}
