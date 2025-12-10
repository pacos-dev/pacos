package org.pacos.core.system.event;

import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.Test;
import org.pacos.core.config.session.UserSessionService;
import org.pacos.core.system.view.login.LoginForm;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogInEventTest {

    @Test
    void whenSessionInitializationFailsThenDoNotNavigateOrSetCookie() {
        LoginForm loginForm = new LoginForm("testLogin", "testPassword");
        UI mockUi = mock(UI.class);
        UserSessionService mockUserService = mock(UserSessionService.class);

        when(mockUserService.initializeSession(loginForm)).thenReturn(false);

        LogInEvent.fireEvent(loginForm, mockUi, mockUserService);

        verify(mockUserService, never()).storeToken(anyString());
        verify(mockUi, never()).navigate(anyString());
    }

    @Test
    void whenRememberMeIsTrueThenStoreTokenAndSetCookie() {
        LoginForm loginForm = new LoginForm("testLogin", "testPassword");
        loginForm.setRememberMe(true);
        UI mockUi = mock(UI.class);
        UserSessionService mockUserService = mock(UserSessionService.class);

        when(mockUserService.initializeSession(loginForm)).thenReturn(true);

        LogInEvent.fireEvent(loginForm, mockUi, mockUserService);

        verify(mockUserService, times(1)).storeToken(anyString());
        verify(mockUi, times(1)).navigate("desktop");
    }

    @Test
    void whenRememberMeIsFalseThenDoNotStoreTokenOrSetCookie() {
        LoginForm loginForm = new LoginForm("testLogin", "testPassword");
        loginForm.setRememberMe(false);
        UI mockUi = mock(UI.class);
        UserSessionService mockUserService = mock(UserSessionService.class);

        when(mockUserService.initializeSession(loginForm)).thenReturn(true);

        LogInEvent.fireEvent(loginForm, mockUi, mockUserService);

        verify(mockUserService, never()).storeToken(anyString());
        verify(mockUi, times(1)).navigate("desktop");
    }
}
