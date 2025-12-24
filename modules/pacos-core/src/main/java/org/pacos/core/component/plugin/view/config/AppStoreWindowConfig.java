package org.pacos.core.component.plugin.view.config;

import com.vaadin.flow.server.VaadinSession;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.plugin.view.window.AppStoreWindow;
import org.pacos.core.component.security.SystemPermissions;
import org.springframework.stereotype.Component;

/**
 * Setting tab configuration for 'plugin manager'
 */
@Component
public class AppStoreWindowConfig implements WindowConfig {

    @Override
    public String title() {
        return "App center";
    }

    @Override
    public String icon() {
        return PacosIcon.APP_STORE.getUrl();
    }

    @Override
    public Class<? extends DesktopWindow> activatorClass() {
        return AppStoreWindow.class;
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
        return VaadinSession.getCurrent()!=null && UserSession.getCurrent().hasPermission(SystemPermissions.APP_STORE_VISIBLE);
    }
}
