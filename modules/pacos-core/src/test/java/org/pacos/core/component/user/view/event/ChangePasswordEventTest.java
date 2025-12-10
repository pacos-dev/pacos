package org.pacos.core.component.user.view.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.user.service.ChangePasswordForm;
import org.pacos.core.system.view.login.LoginForm;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChangePasswordEventTest {

    private UserProxyService userProxyService;

    @BeforeEach
    void setUp() {
        userProxyService = mock(UserProxyService.class);
        UserSession mockSession = mock(UserSession.class);
        when(UserSession.getCurrent()).thenReturn(mockSession);
        when(mockSession.getUserName()).thenReturn("testUser");
    }

    @Test
    void whenValidCredentialsAndPasswordChangedThenSuccessNotification() {
        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("oldPassword");
        form.setNewPassword("newPassword");
        form.setRepeatPassword("newPassword");

        LoginForm loginForm = new LoginForm();
        loginForm.setLogin("testUser");
        loginForm.setPassword("oldPassword");

        when(userProxyService.isValidCredentials(Mockito.any(LoginForm.class))).thenReturn(true);
        when(userProxyService.changePassword(Mockito.any(LoginForm.class), eq(form))).thenReturn(true);

        ChangePasswordEvent.fireEvent(userProxyService, form);

        verify(userProxyService).isValidCredentials(any(LoginForm.class));
        verify(userProxyService).changePassword(any(LoginForm.class), eq(form));
    }

    @Test
    void whenInvalidCredentialsThenErrorNotification() {
        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("wrongPassword");
        form.setNewPassword("newPassword");
        form.setRepeatPassword("newPassword");

        when(userProxyService.isValidCredentials(Mockito.any(LoginForm.class))).thenReturn(false);

        ChangePasswordEvent.fireEvent(userProxyService, form);

        verify(userProxyService).isValidCredentials(any(LoginForm.class));
        verify(userProxyService, never()).changePassword(any(LoginForm.class), any(ChangePasswordForm.class));
    }
}
