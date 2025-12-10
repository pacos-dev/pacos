package org.pacos.core.system.window.config;

import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.system.window.SystemInfoConfigPanel;
import org.springframework.stereotype.Component;

@Component
public class SystemInfoConfig implements WindowConfig {

    @Override
    public String title() {
        return "System info";
    }

    @Override
    public String icon() {
        return PacosIcon.SYSTEM_INFO.getUrl();
    }

    @Override
    public Class<? extends DesktopWindow> activatorClass() {
        return SystemInfoConfigPanel.class;
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
