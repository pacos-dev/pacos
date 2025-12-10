package org.pacos.core.config.session;

import java.util.Optional;

import jakarta.servlet.http.Cookie;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.manager.WindowManager;
import org.pacos.core.component.security.service.UserPermissionService;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.user.service.UserService;
import org.pacos.core.system.cookie.CookieUtils;
import org.pacos.core.system.view.login.LoginForm;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSessionServiceTest {

    @Mock
    private UserProxyService userProxyService;
    @Mock
    private UserPermissionService userPermissionService;
    @Mock
    private VaadinSession vaadinSession;
    @Mock
    private UserService userService;
    @Mock
    private UI ui;

    @InjectMocks
    private UserSessionService userSessionService;

    @BeforeEach
    void setUp() {
        UI.setCurrent(ui);
        VaadinSession.setCurrent(vaadinSession);
    }

    @Test
    void whenIsLogInThenReturnTrue() {
        UserSession userSession = new UserSession(new UserDTO("admin"));
        Mockito.when(vaadinSession.getAttribute(UserSession.class)).thenReturn(userSession);

        boolean result = UserSessionService.isLogIn(userProxyService);

        assertTrue(result);
    }

    @Test
    void whenIsLogInThenReturnFalseAndAutoLogin() {
        Mockito.when(vaadinSession.getAttribute(UserSession.class)).thenReturn(null);
        Mockito.when(userProxyService.isSingleUserMode()).thenReturn(true);

        boolean result = UserSessionService.isLogIn(userProxyService);

        assertTrue(result);
    }

    @Test
    void whenLogOutThenDestroySession() {
        UserSession userSession = new UserSession(new UserDTO("admin"));
        Mockito.when(vaadinSession.getAttribute(UserSession.class)).thenReturn(userSession);

        UserSessionService.logOut();

        verify(vaadinSession).setAttribute(UserSession.class, null);
        verify(vaadinSession).close();
    }

    @Test
    void whenInitializeGuestSessionThenSetGuestSession() {
        Mockito.when(vaadinSession.getAttribute(UserSession.class)).thenReturn(null);

        userSessionService.initializeGuestSession();

        verify(vaadinSession).setAttribute(Mockito.eq(UserSession.class), Mockito.any(UserSession.class));
    }

    @Test
    void whenDestroySessionThenDetachAllOpenedWindows() {
        UserSession session = new UserSession(new UserDTO("admin"));
        UISystem uiSystem = mock(UISystem.class);
        when(uiSystem.getWindowManager()).thenReturn(mock(WindowManager.class));
        session.setUiSystem(uiSystem);

        when(vaadinSession.getAttribute(UserSession.class)).thenReturn(session);
        doNothing().when(vaadinSession).setAttribute(UserSession.class, null);
        //when
        UserSessionService.destroyUserSession();
        //then
        verify(uiSystem.getWindowManager()).detachAll();
    }


    @Test
    void whenAutoLoginThenReturnTrue() {
        Cookie cookie = new Cookie(CookieUtils.TOKEN, "valid-token");
        try (MockedStatic<CookieUtils> cookieMock = Mockito.mockStatic(CookieUtils.class)) {
            cookieMock.when(() -> CookieUtils.getCookie(CookieUtils.TOKEN))
                    .thenReturn(Optional.of(cookie));
            when(userProxyService.getUserPermissionService()).thenReturn(userPermissionService);
            Mockito.when(userProxyService.loadUserByToken("valid-token"))
                    .thenReturn(Optional.of(new UserDTO("admin")));
            boolean result = UserSessionService.autoLogin(userProxyService);

            assertTrue(result);
        }
    }

    @Test
    void whenAutoLoginThenReturnFalse() {
        try (MockedStatic<CookieUtils> cookieMock = Mockito.mockStatic(CookieUtils.class)) {
            cookieMock.when(() -> CookieUtils.getCookie(CookieUtils.TOKEN))
                    .thenReturn(Optional.empty());

            boolean result = UserSessionService.autoLogin(userProxyService);

            assertFalse(result);
        }
    }

    @Test
    void whenInitializeSessionWithValidCredentialsThenReturnTrue() {
        LoginForm loginForm = new LoginForm("admin", "password");
        Mockito.when(userService.isValidCredentials(loginForm)).thenReturn(true);
        Mockito.when(vaadinSession.getAttribute(UserSession.class))
                .thenReturn(null, new UserSession(new UserDTO("admin")));
        Mockito.when(userService.loadUserByLogin("admin"))
                .thenReturn(Optional.of(new UserDTO("admin")));

        boolean result = userSessionService.initializeSession(loginForm);

        assertTrue(result);
    }

    @Test
    void whenInitializeSessionWithInvalidCredentialsThenReturnFalse() {
        LoginForm loginForm = new LoginForm("admin", "wrong-password");
        Mockito.when(userService.isValidCredentials(loginForm)).thenReturn(false);

        boolean result = userSessionService.initializeSession(loginForm);

        assertFalse(result);
    }

    @Test
    void whenStoreTokenThenInvokeSaveToken() {
        when(vaadinSession.getAttribute(UserSession.class))
                .thenReturn(new UserSession(new UserDTO("admin")));

        userSessionService.storeToken("valid-token");

        verify(userService).saveToken("valid-token", UserSession.getCurrent().getUser());
    }

    @Test
    void whenIsValidCredentialsThenCallProxy() {
        LoginForm loginForm = new LoginForm("login", "pass");
        //when
        userSessionService.isValidCredentials(loginForm);
        //then
        verify(userService).isValidCredentials(loginForm);
    }

    @Test
    void whenInitializeGuestSessionThenReturnTrue() {
        //given
        VaadinSession session = mock(VaadinSession.class);
        VaadinSession.setCurrent(session);
        //when
        userSessionService.initializeGuestSession();
        //then
        ArgumentCaptor<Class> classArgumentCaptor = ArgumentCaptor.forClass(Class.class);
        ArgumentCaptor<UserSession> sessionArgumentCaptor = ArgumentCaptor.forClass(UserSession.class);
        verify(session).setAttribute(classArgumentCaptor.capture(), sessionArgumentCaptor.capture());

        assertTrue(sessionArgumentCaptor.getValue().getUser().isGuestSession());
    }
}
