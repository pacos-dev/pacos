package org.pacos.core.system.event;

import com.vaadin.flow.component.UI;
import org.pacos.core.component.user.proxy.UserProxyService;

public final class InitializeGuestSessionEvent {

    private InitializeGuestSessionEvent() {
    }

    public static void fireEvent(UserProxyService userSessionService, UI ui) {
        userSessionService.initializeGuestSession();
        ui.navigate("desktop");
    }
}
