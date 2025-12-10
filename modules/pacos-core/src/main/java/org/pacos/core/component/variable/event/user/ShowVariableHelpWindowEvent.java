package org.pacos.core.component.variable.event.user;

import org.pacos.core.component.variable.system.user.UserVariableSystem;
import org.pacos.core.component.variable.view.config.VariableHelpConfig;

public final class ShowVariableHelpWindowEvent {
    private ShowVariableHelpWindowEvent() {
    }

    public static void fireEvent(UserVariableSystem system) {
        system.getUiSystem().getWindowManager().showWindow(VariableHelpConfig.class);
    }
}
