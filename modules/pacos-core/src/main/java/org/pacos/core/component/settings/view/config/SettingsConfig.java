package org.pacos.core.component.settings.view.config;

import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.settings.view.PanelSettings;
import org.springframework.stereotype.Component;

@Component
public class SettingsConfig implements WindowConfig {

    @Override
    public String title() {
        return "Settings";
    }

    @Override
    public String icon() {
        return PacosIcon.SETTINGS.getUrl();
    }

    @Override
    public Class<? extends DesktopWindow> activatorClass() {
        return PanelSettings.class;
    }

    @Override
    public boolean isApplication() {
        return true;
    }

    @Override
    public boolean isAllowMultipleInstance() {
        return false;
    }

    @Override
    public boolean isAllowedForCurrentSession(UserSession userSession) {
        return userSession.hasPermission(SystemPermissions.SETTINGS_PLUGIN_VISIBLE);
    }
}
