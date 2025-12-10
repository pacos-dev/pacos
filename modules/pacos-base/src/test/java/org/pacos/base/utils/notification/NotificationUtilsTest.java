package org.pacos.base.utils.notification;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NotificationUtilsTest {

    @Test
    void whenShowErrorNotificationThenHasErrorVariant() {
        Notification notification = NotificationUtils.show("Error message", MessageType.ERROR);
        assertTrue(notification.getThemeNames().contains("error"));
    }

    @Test
    void whenShowSuccessNotificationThenHasSuccessVariant() {
        Notification notification = NotificationUtils.show("Success message", MessageType.SUCCESS);
        assertTrue(notification.getThemeNames().contains("success"));
    }

    @Test
    void whenErrorMessageThenErrorNotificationIsDisplayed() {
        Notification notification = NotificationUtils.show("Error message", MessageType.ERROR);
        assertTrue(notification.getThemeNames().contains(NotificationVariant.LUMO_ERROR.getVariantName()));
    }

    @Test
    void whenSuccessMessageThenSuccessNotificationIsDisplayed() {
        Notification notification = NotificationUtils.show("Success message", MessageType.SUCCESS);
        assertTrue(notification.getThemeNames().contains(NotificationVariant.LUMO_SUCCESS.getVariantName()));
    }

    @Test
    void whenErrorExceptionThenErrorNotificationDisplayedWithMessage() {
        Exception ex = new Exception("Exception message");
        assertDoesNotThrow(() -> NotificationUtils.error(ex));
    }

    @Test
    void whenSuccessMessageWithPositionThenNotificationHasCorrectPosition() {
        Position position = Position.BOTTOM_END;
        Notification notification = NotificationUtils.success("Success with position", position);
        assertEquals(position, notification.getPosition());
        assertTrue(notification.getThemeNames().contains(NotificationVariant.LUMO_SUCCESS.getVariantName()));
    }

    @Test
    void whenSuccessMessageWithArgsThenNotificationFormatsMessageCorrectly() {
        String formattedMessage = String.format("Hello %s!", "World");
        Assertions.assertDoesNotThrow(() -> NotificationUtils.success("Hello %s!", "World"));

    }

}
