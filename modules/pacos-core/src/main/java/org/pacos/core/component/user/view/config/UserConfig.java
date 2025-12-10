package org.pacos.core.component.user.view.config;

import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.user.view.UserConfigPanel;
import org.springframework.stereotype.Component;

@Component
public class UserConfig implements WindowConfig {

    @Override
    public String title() {
        return "User config";
    }

    @Override
    public String icon() {
        return PacosIcon.USER_SETTING.getUrl();
    }

    @Override
    public Class<? extends DesktopWindow> activatorClass() {
        return UserConfigPanel.class;
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
