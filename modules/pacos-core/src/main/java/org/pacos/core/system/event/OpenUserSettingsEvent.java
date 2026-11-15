package org.pacos.core.system.event;

import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.user.view.config.UserGuestSessionConfig;
import org.pacos.core.component.user.view.config.UserPasswordConfig;

import com.vaadin.flow.component.notification.Notification;

public final class OpenUserSettingsEvent {

    private OpenUserSettingsEvent() {
    }

    public static void fireEvent(UISystem system, RegistryProxy registryProxy) {
        if (UserSession.getCurrent().getUser().isGuestSession()) {
            if(registryProxy.isRegistrationPanelEnabled()) {
                system.getWindowManager().showWindow(UserGuestSessionConfig.class);
            }else{
                Notification.show("User management is disabled for Guest account",5000, Notification.Position.TOP_CENTER);
            }
        } else {
            system.getWindowManager().showWindow(UserPasswordConfig.class);
        }
    }
}
