package org.pacos.core.component.variable.event.global;

import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.system.global.GlobalVariableEvent;
import org.pacos.core.component.variable.system.global.GlobalVariableSystem;

public final class EditVariableItemEvent {

    private EditVariableItemEvent() {
    }

    public static void fireEvent(SystemVariableDTO dto, GlobalVariableSystem system) {
        system.notify(GlobalVariableEvent.OPEN_GLOBAL_VARIABLE_TAB, dto);
    }
}
