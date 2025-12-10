package org.pacos.core.component.variable.event.global;

import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.system.global.GlobalVariableEvent;
import org.pacos.core.component.variable.system.global.GlobalVariableSystem;

public final class AddNewGlobalVariableEvent {

    private AddNewGlobalVariableEvent() {
    }

    public static void fireEvent(GlobalVariableSystem system) {
        SystemVariableDTO newVariable = system.getSystemVariableProxy().initNewVariable();
        system.notify(GlobalVariableEvent.ADD_NEW_VARIABLE_TO_GRID, newVariable);
        system.notify(GlobalVariableEvent.OPEN_GLOBAL_VARIABLE_TAB, newVariable);
    }
}
