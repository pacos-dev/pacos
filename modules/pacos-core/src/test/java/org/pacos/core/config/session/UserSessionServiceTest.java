package org.pacos.core.config.session;

import java.util.Optional;
import java.util.Set;

import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.VaadinSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.system.view.login.LoginForm;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserSessionServiceTest {

    private VaadinSession vaadinSession;
    private UserProxyService userProxyService;
    private UserSessionService service;

    @BeforeEach
    void setUp() {
        CurrentInstance.clearAll();
        vaadinSession = mock(VaadinSession.class);
        VaadinSession.setCurrent(vaadinSession);
        userProxyService = mock(UserProxyService.class);
        service = new UserSessionService(userProxyService);
    }

    @AfterEach
    void tearDown() {
        CurrentInstance.clearAll();
    }

    @Test
    void whenInitializeGuestSessionWithoutExistingSessionThenCreateGuestSession() {
        when(vaadinSession.getAttribute(UserSession.class)).thenReturn(null);
        when(userProxyService.loadUserPermissions(UserDTO.GUEST_ID)).thenReturn(Set.of());

        service.initializeGuestSession();

        ArgumentCaptor<UserSession> captor = ArgumentCaptor.forClass(UserSession.class);
        verify(vaadinSession).setAttribute(eq(UserSession.class), captor.capture());
        assertTrue(captor.getValue().getUser().isGuestSession());
        assertTrue(captor.getValue().getUserName().startsWith("Guest "));
    }

    @Test
    void whenInitializeGuestSessionWithExistingSessionThenKeepExistingSession() {
        UserSession existing = new UserSession(new UserDTO(7, "existing", null));
        when(vaadinSession.getAttribute(UserSession.class)).thenReturn(existing);

        service.initializeGuestSession();

        verify(vaadinSession, never()).setAttribute(eq(UserSession.class), any(UserSession.class));
        verify(userProxyService, never()).loadUserPermissions(any(Integer.class));
    }

    @Test
    void whenInitializeSessionWithInvalidCredentialsThenReturnFalse() {
        LoginForm loginForm = new LoginForm("admin", "invalid");
        when(userProxyService.isValidCredentials(loginForm)).thenReturn(false);

        assertFalse(service.initializeSession(loginForm));

        verify(userProxyService, never()).loadUserByLogin("admin");
    }

    @Test
    void whenInitializeSessionWithValidCredentialsAndKnownUserThenCreateSession() {
        LoginForm loginForm = new LoginForm("admin", "password");
        UserDTO user = new UserDTO(UserDTO.ADMIN_ID, "admin", 0);
        when(userProxyService.isValidCredentials(loginForm)).thenReturn(true);
        when(vaadinSession.getAttribute(UserSession.class)).thenReturn(null, new UserSession(user));
        when(userProxyService.loadUserByLogin("admin")).thenReturn(Optional.of(user));
        when(userProxyService.loadUserPermissions(UserDTO.ADMIN_ID)).thenReturn(Set.of());

        assertTrue(service.initializeSession(loginForm));

        verify(vaadinSession).setAttribute(eq(UserSession.class), any(UserSession.class));
        verify(userProxyService).loadUserPermissions(UserDTO.ADMIN_ID);
    }

    @Test
    void whenInitializeSessionWithValidCredentialsAndUnknownUserThenReturnFalse() {
        LoginForm loginForm = new LoginForm("missing", "password");
        when(userProxyService.isValidCredentials(loginForm)).thenReturn(true);
        when(vaadinSession.getAttribute(UserSession.class)).thenReturn(null, null);
        when(userProxyService.loadUserByLogin("missing")).thenReturn(Optional.empty());

        assertFalse(service.initializeSession(loginForm));

        verify(vaadinSession, never()).setAttribute(eq(UserSession.class), any(UserSession.class));
    }

    @Test
    void whenIsLogInCalledWithoutSessionAndSingleUserModeDisabledThenReturnFalse() {
        when(vaadinSession.getAttribute(UserSession.class)).thenReturn(null);
        when(userProxyService.isSingleUserMode()).thenReturn(false);

        assertFalse(UserSessionService.isLogIn(userProxyService));

        verify(userProxyService).isSingleUserMode();
    }

    @Test
    void whenIsLogInCalledWithoutSessionAndSingleUserModeEnabledThenCreateAdminSession() {
        UserDTO admin = new UserDTO(UserDTO.ADMIN_ID, "admin", 0);
        when(vaadinSession.getAttribute(UserSession.class)).thenReturn(null);
        when(userProxyService.isSingleUserMode()).thenReturn(true);
        when(userProxyService.loadUserByLogin("admin")).thenReturn(Optional.of(admin));
        when(userProxyService.loadUserPermissions(UserDTO.ADMIN_ID)).thenReturn(Set.of());

        assertTrue(UserSessionService.isLogIn(userProxyService));

        verify(vaadinSession).setAttribute(eq(UserSession.class), any(UserSession.class));
        verify(userProxyService).loadUserPermissions(UserDTO.ADMIN_ID);
    }

    @Test
    void whenIsLogInCalledWithExistingSessionThenReturnTrue() {
        when(vaadinSession.getAttribute(UserSession.class))
                .thenReturn(new UserSession(new UserDTO(UserDTO.ADMIN_ID, "admin", 0)));

        assertTrue(UserSessionService.isLogIn(userProxyService));

        verify(userProxyService, never()).isSingleUserMode();
    }

    @Test
    void whenDestroyUserSessionCalledThenClearSessionAttribute() {
        when(vaadinSession.getAttribute(UserSession.class))
                .thenReturn(new UserSession(new UserDTO(8, "user", null)));

        UserSessionService.destroyUserSession();

        verify(vaadinSession).setAttribute(UserSession.class, null);
    }

    @Test
    void whenStoreTokenCalledThenSaveTokenForCurrentUser() {
        UserDTO user = new UserDTO(9, "user", null);
        when(vaadinSession.getAttribute(UserSession.class)).thenReturn(new UserSession(user));

        service.storeToken("token-value");

        verify(userProxyService).saveToken("token-value", user);
    }

    @Test
    void whenIsValidCredentialsCalledThenDelegateToUserProxyService() {
        LoginForm loginForm = new LoginForm("user", "password");
        when(userProxyService.isValidCredentials(loginForm)).thenReturn(true);

        assertTrue(service.isValidCredentials(loginForm));

        verify(userProxyService).isValidCredentials(loginForm);
    }
}
