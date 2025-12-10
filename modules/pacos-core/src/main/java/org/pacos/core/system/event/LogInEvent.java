package org.pacos.core.system.event;

import java.util.UUID;

import com.vaadin.flow.component.UI;
import org.pacos.core.config.session.UserSessionService;
import org.pacos.core.system.cookie.CookieUtils;
import org.pacos.core.system.view.login.LoginForm;

public final class LogInEvent {

    private LogInEvent() {
    }

    public static void fireEvent(LoginForm loginForm, UI ui, UserSessionService userService) {
        if (!userService.initializeSession(loginForm)) {
            return;
        }

        if (loginForm.getRememberMe()) {
            final String value = UUID.randomUUID() + "-" + UUID.randomUUID();
            userService.storeToken(value);
            CookieUtils.setCookie(CookieUtils.TOKEN, value, Integer.MAX_VALUE);
        }

        ui.navigate("desktop");
    }
}
