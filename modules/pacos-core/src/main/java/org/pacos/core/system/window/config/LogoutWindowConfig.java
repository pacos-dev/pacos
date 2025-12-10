package org.pacos.core.system.window.config;

import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.system.window.LogoutWindow;
import org.springframework.stereotype.Component;

@Component
public class LogoutWindowConfig implements WindowConfig {

    @Override
    public String title() {
        return "Logout";
    }

    @Override
    public String icon() {
        return PacosIcon.LOGOUT.getUrl();
    }

    @Override
    public Class<? extends DesktopWindow> activatorClass() {
        return LogoutWindow.class;
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
