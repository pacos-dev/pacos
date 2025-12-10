package org.pacos.core.system.event;

import com.vaadin.flow.component.notification.Notification;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.user.view.config.UserGuestSessionConfig;
import org.pacos.core.component.variable.view.plugin.VariablePlugin;

public final class OpenVariablePluginEvent {

    private OpenVariablePluginEvent() {

    }

    public static void fireEvent(UISystem uiSystem, VariablePlugin variablePlugin, RegistryProxy registryProxy) {
        if (UserSession.getCurrent().getUser().isGuestSession()) {
            if (registryProxy.isRegistrationPanelEnabled()) {
                Notification.show("Variable management is allowed ony for logged user. Create an account if you wish");
                uiSystem.getWindowManager().showWindow(UserGuestSessionConfig.class);
            } else {
                Notification.show("Variable management is disabled for Guest account", 5000, Notification.Position.TOP_CENTER);
            }
        } else {
            variablePlugin.open();
        }
    }
}
