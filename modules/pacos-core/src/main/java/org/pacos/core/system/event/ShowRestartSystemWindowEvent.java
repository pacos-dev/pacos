package org.pacos.core.system.event;

import org.pacos.base.event.UISystem;
import org.pacos.base.window.DesktopWindow;
import org.pacos.core.system.window.config.RestartWindowConfig;

public final class ShowRestartSystemWindowEvent {

    private ShowRestartSystemWindowEvent() {
    }

    public static void fireEvent(UISystem system) {
        DesktopWindow dw = system.getWindowManager().showWindow(RestartWindowConfig.class);
        system.getWindowManager().showAndMoveToFront(dw);
    }
}
