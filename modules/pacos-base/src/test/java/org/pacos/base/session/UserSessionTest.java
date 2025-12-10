package org.pacos.base.session;

import java.util.Set;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pacos.base.event.UISystem;
import org.pacos.base.security.Permission;
import org.pacos.base.security.PermissionConfig;
import org.pacos.base.utils.notification.NotificationUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserSessionTest {
    private UserDTO mockUser;
    private UserSession userSession;
    private Permission mockPermission;
    private VaadinSession mockVaadinSession;
    private UI mockUI;
    private UISystem mockUISystem;

    @BeforeEach
    void setUp() {
        mockUser = mock(UserDTO.class);
        mockVaadinSession = mock(VaadinSession.class);
        mockUI = mock(UI.class);
        mockUISystem = mock(UISystem.class);
        mockPermission = mock(Permission.class);

        when(mockUser.getId()).thenReturn(123);
        when(mockUser.getUserName()).thenReturn("testUser");

        userSession = new UserSession(mockUser);
    }

    @Test
    void whenGetUserIdCalledThenReturnUserId() {
        assertEquals(123, userSession.getUserId());
    }

    @Test
    void whenGetUserNameCalledThenReturnUserName() {
        assertEquals("testUser", userSession.getUserName());
    }

    @Test
    void whenAddToSessionThenValueIsStored() {
        userSession.addToSession("testKey", "testValue");
        assertEquals("testValue", userSession.getFromSession("testKey"));
    }

    @Test
    void whenRemoveFromSessionThenValueIsRemoved() {
        userSession.addToSession("testKey", "testValue");
        userSession.removeFromSession("testKey");
        assertNull(userSession.getFromSession("testKey"));
    }

    @Test
    void whenSetUiSystemThenSystemIsStoredForCurrentUI() {
        try (MockedStatic<UI> mockedUI = Mockito.mockStatic(UI.class)) {
            mockedUI.when(UI::getCurrent).thenReturn(mockUI);
            userSession.setUiSystem(mockUISystem);
            assertEquals(mockUISystem, userSession.getUISystem());
        }
    }

    @Test
    void whenGetUISystemWithSpecificUIThenReturnCorrectSystem() {
        userSession.setUiSystem(mockUISystem);
        assertNull(userSession.getUISystem(mockUI));

        try (MockedStatic<UI> mockedUI = Mockito.mockStatic(UI.class)) {
            mockedUI.when(UI::getCurrent).thenReturn(mockUI);
            userSession.setUiSystem(mockUISystem);
            assertEquals(mockUISystem, userSession.getUISystem(mockUI));
        }
    }

    @Test
    void whenGetCurrentUserSessionThenReturnUserSessionFromVaadinSession() {
        try (MockedStatic<VaadinSession> mockedVaadinSession = Mockito.mockStatic(VaadinSession.class)) {
            mockedVaadinSession.when(VaadinSession::getCurrent).thenReturn(mockVaadinSession);
            when(mockVaadinSession.getAttribute(UserSession.class)).thenReturn(userSession);
            assertEquals(userSession, UserSession.getCurrent());
        }
    }

    @Test
    void whenGetUserThenReturnUser() {
        assertEquals(mockUser, userSession.getUser());
    }

    @Test
    void whenAdminSessionThenHasPermissionReturnsTrue() {
        // given
        when(mockUser.isAdminSession()).thenReturn(true);
        userSession = new UserSession(mockUser);
        when(mockPermission.getKey()).thenReturn("test.permission");

        // when
        boolean hasPermission = userSession.hasPermission(mockPermission);

        // then
        assertTrue(hasPermission);
    }

    @Test
    void whenPermissionConfiguredThenHasPermissionReturnsTrue() {
        // given
        when(mockUser.isAdminSession()).thenReturn(false);
        when(mockPermission.getKey()).thenReturn("test.permission");
        PermissionConfig permissionConfig = new PermissionConfig("test.permission", "ALLOW");
        userSession = new UserSession(mockUser, Set.of(permissionConfig));

        // when
        boolean hasPermission = userSession.hasPermission(mockPermission);

        // then
        assertTrue(hasPermission);
    }

    @Test
    void whenPermissionNotConfiguredThenHasPermissionReturnsFalse() {
        // given
        when(mockUser.isAdminSession()).thenReturn(false);
        when(mockPermission.getKey()).thenReturn("test.permission");
        userSession = new UserSession(mockUser, Set.of());

        // when
        boolean hasPermission = userSession.hasPermission(mockPermission);

        // then
        assertFalse(hasPermission);
    }

    @Test
    void whenHasActionPermissionDeniedThenNotificationShownAndReturnsFalse() {
        // given
        when(mockUser.isAdminSession()).thenReturn(false);
        when(mockPermission.getKey()).thenReturn("test.permission");
        userSession = new UserSession(mockUser, Set.of());
        try (MockedStatic<VaadinSession> mockedVaadinSession = Mockito.mockStatic(VaadinSession.class);
             MockedStatic<NotificationUtils> mockedNotification = Mockito.mockStatic(NotificationUtils.class)) {
            mockedVaadinSession.when(VaadinSession::getCurrent).thenReturn(mockVaadinSession);

            // when
            boolean hasActionPermission = userSession.hasActionPermission(mockPermission);

            // then
            assertFalse(hasActionPermission);
            mockedNotification.verify(() -> NotificationUtils.info("You don't have permission for this action: test.permission"));
        }
    }

    @Test
    void whenHasActionPermissionAllowedThenNotificationNotShownAndReturnsTrue() {
        // given
        when(mockUser.isAdminSession()).thenReturn(false);
        when(mockPermission.getKey()).thenReturn("test.permission");
        PermissionConfig permissionConfig = new PermissionConfig("test.permission", "ALLOW");
        userSession = new UserSession(mockUser, Set.of(permissionConfig));
        try (MockedStatic<VaadinSession> mockedVaadinSession = Mockito.mockStatic(VaadinSession.class);
             MockedStatic<NotificationUtils> mockedNotification = Mockito.mockStatic(NotificationUtils.class)) {
            mockedVaadinSession.when(VaadinSession::getCurrent).thenReturn(mockVaadinSession);

            // when
            boolean hasActionPermission = userSession.hasActionPermission(mockPermission);

            // then
            assertTrue(hasActionPermission);
            mockedNotification.verifyNoInteractions();
        }
    }
}