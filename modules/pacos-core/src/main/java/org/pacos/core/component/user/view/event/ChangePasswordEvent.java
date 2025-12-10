package org.pacos.core.component.user.view.event;

import org.pacos.base.session.UserSession;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.user.service.ChangePasswordForm;
import org.pacos.core.system.view.login.LoginForm;

public final class ChangePasswordEvent {

    private ChangePasswordEvent() {
    }

    public static void fireEvent(UserProxyService userProxyService, ChangePasswordForm changePasswordForm) {
        LoginForm loginForm = new LoginForm();
        loginForm.setLogin(UserSession.getCurrent().getUserName());
        loginForm.setPassword(changePasswordForm.getCurrentPassword());

        if (userProxyService.isValidCredentials(loginForm)) {
            if (userProxyService.changePassword(loginForm, changePasswordForm)) {
                NotificationUtils.success("Password changed");
            }
        } else {
            NotificationUtils.error("Current password is invalid");
        }
    }
}
