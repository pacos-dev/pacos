package org.pacos.base.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

class NotificationRequestTest {

    @Test
    void defaultsToAllUsersAndNormalizesSourceKey() {
        NotificationRequest request = new NotificationRequest(
                " app_center ", null, "Calendar plugin", null, null,
                null, null, null, null, false, null, null);

        assertEquals("APP_CENTER", request.sourceKey());
        assertEquals(NotificationAudience.ALL_USERS, request.audience());
        assertEquals(Set.of(), request.recipientUserIds());
        assertEquals(NotificationCategory.SYSTEM, request.category());
        assertEquals(NotificationPriority.INFO, request.priority());
    }

    @Test
    void selectedUsersRequireAtLeastOneRecipient() {
        assertThrows(IllegalArgumentException.class, () -> new NotificationRequest(
                "APP_CENTER", "plugin:calendar:capabilities", "Calendar plugin", null, null,
                null, null, null, null, false, NotificationAudience.SELECTED_USERS, Set.of()));
    }

    @Test
    void allUsersCannotBeCombinedWithExplicitRecipients() {
        assertThrows(IllegalArgumentException.class, () -> new NotificationRequest(
                "APP_CENTER", null, "Calendar plugin", null, null,
                null, null, null, null, false, NotificationAudience.ALL_USERS, Set.of(7)));
    }

    @Test
    void blankBusinessKeyIsRejectedRatherThanDisablingDeduplicationAccidentally() {
        assertThrows(IllegalArgumentException.class, () -> new NotificationRequest(
                "APP_CENTER", "  ", "Calendar plugin", null, null,
                null, null, null, null, false, NotificationAudience.ALL_USERS, Set.of()));
    }
}
