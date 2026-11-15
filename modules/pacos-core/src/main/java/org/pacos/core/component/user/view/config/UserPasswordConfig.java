package org.pacos.core.component.user.view.config;

import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.user.view.UserChangePasswordPanel;
import org.springframework.stereotype.Component;

/** Configures the window used to change the current user's password. */
@Component
public class UserPasswordConfig implements WindowConfig {

    @Override
    public String title() {
        return "Change password";
    }

    @Override
    public String icon() {
        return PacosIcon.USER_SETTING.getUrl();
    }

    @Override
    public Class<? extends DesktopWindow> activatorClass() {
        return UserChangePasswordPanel.class;
    }

    @Override
    public boolean isApplication() {
        return false;
    }

    @Override
    public boolean isAllowMultipleInstance() {
        return false;
    }
}
