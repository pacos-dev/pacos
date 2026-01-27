package org.pacos.core.system.event;

import org.pacos.core.config.session.UserSessionService;

import com.vaadin.flow.component.UI;

public final class InitializeGuestSessionEvent {

    private InitializeGuestSessionEvent() {
    }

    public static void fireEvent(UserSessionService userSessionService, UI ui) {
        userSessionService.initializeGuestSession();
        ui.navigate("desktop");
    }
}
