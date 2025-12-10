package org.pacos.core.system.window.config;

import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.system.window.ReleaseNotePanel;
import org.springframework.stereotype.Component;

@Component
public class ReleaseNoteConfig implements WindowConfig {

    @Override
    public String title() {
        return "Release note";
    }

    @Override
    public String icon() {
        return PacosIcon.SYSTEM_INFO.getUrl();
    }

    @Override
    public Class<? extends DesktopWindow> activatorClass() {
        return ReleaseNotePanel.class;
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
