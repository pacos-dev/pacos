package org.pacos.core.system.event;

import org.pacos.base.event.UISystem;
import org.pacos.core.system.window.config.SystemInfoConfig;

public final class OpenSystemInfoEvent {
    private OpenSystemInfoEvent() {
    }

    public static void fireEvent(UISystem system) {
        system.getWindowManager().showWindow(SystemInfoConfig.class);
    }
}
