package org.pacos.core.component.variable.view.config;

import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.variable.view.PanelVariable;
import org.springframework.stereotype.Component;

@Component
public class VariableConfig implements WindowConfig {

    @Override
    public String title() {
        return "Variables";
    }

    @Override
    public String icon() {
        return PacosIcon.VARIABLE.getUrl();
    }

    @Override
    public Class<? extends DesktopWindow> activatorClass() {
        return PanelVariable.class;
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
        return !userSession.getUser().isGuestSession();
    }
}
