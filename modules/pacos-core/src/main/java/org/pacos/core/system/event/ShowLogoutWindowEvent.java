package org.pacos.core.system.event;

import org.pacos.base.event.UISystem;
import org.pacos.base.window.DesktopWindow;
import org.pacos.core.system.window.config.LogoutWindowConfig;

public final class ShowLogoutWindowEvent {

    private ShowLogoutWindowEvent() {
    }

    public static void fireEvent(UISystem system) {
        DesktopWindow dw = system.getWindowManager().showWindow(LogoutWindowConfig.class);
        UISystem.getCurrent().getWindowManager().showAndMoveToFront(dw);
    }
}
