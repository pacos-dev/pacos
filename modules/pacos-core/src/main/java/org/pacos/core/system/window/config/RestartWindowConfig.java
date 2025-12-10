package org.pacos.core.system.window.config;

import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.system.window.RestartWindow;
import org.springframework.stereotype.Component;

@Component
public class RestartWindowConfig implements WindowConfig {

    @Override
    public String title() {
        return "Restart";
    }

    @Override
    public String icon() {
        return PacosIcon.LOGOUT.getUrl();
    }

    @Override
    public Class<? extends DesktopWindow> activatorClass() {
        return RestartWindow.class;
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
