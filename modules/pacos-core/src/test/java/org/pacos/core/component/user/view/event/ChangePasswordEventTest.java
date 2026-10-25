package org.pacos.core.component.user.view.event;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pacos.base.session.UserDTO;
import org.pacos.base.utils.notification.NotificationUtils;
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
        VaadinMock.mockSystem(new UserDTO(2, "testUser", 0));
        userProxyService = mock(UserProxyService.class);
    }

    @Test
    void whenValidCredentialsAndPasswordChangedThenSuccessNotification() {
        try (MockedStatic<NotificationUtils> notificationUtils = Mockito.mockStatic(NotificationUtils.class)) {
            ChangePasswordForm form = new ChangePasswordForm();
            form.setCurrentPassword("oldPassword");
            form.setNewPassword("newPassword");
            form.setRepeatPassword("newPassword");

            when(userProxyService.isValidCredentials(Mockito.any(LoginForm.class))).thenReturn(true);
            when(userProxyService.changePassword(Mockito.any(LoginForm.class), eq(form))).thenReturn(true);

            ChangePasswordEvent.fireEvent(userProxyService, form);

            verify(userProxyService).isValidCredentials(any(LoginForm.class));
            verify(userProxyService).changePassword(any(LoginForm.class), eq(form));
            notificationUtils.verify(() -> NotificationUtils.success("Password changed"));
        }
    }

    @Test
    void whenInvalidCredentialsThenErrorNotification() {
        try (MockedStatic<NotificationUtils> notificationUtils = Mockito.mockStatic(NotificationUtils.class)) {
            ChangePasswordForm form = new ChangePasswordForm();
            form.setCurrentPassword("wrongPassword");
            form.setNewPassword("newPassword");
            form.setRepeatPassword("newPassword");

            when(userProxyService.isValidCredentials(Mockito.any(LoginForm.class))).thenReturn(false);

            ChangePasswordEvent.fireEvent(userProxyService, form);

            verify(userProxyService).isValidCredentials(any(LoginForm.class));
            verify(userProxyService, never()).changePassword(any(LoginForm.class), any(ChangePasswordForm.class));
            notificationUtils.verify(() -> NotificationUtils.error("Current password is invalid"));
        }
    }
}