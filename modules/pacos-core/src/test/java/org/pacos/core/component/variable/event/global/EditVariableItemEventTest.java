package org.pacos.core.component.variable.event.global;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.system.global.GlobalVariableEvent;
import org.pacos.core.component.variable.system.global.GlobalVariableSystem;

import static org.mockito.Mockito.verify;

class EditVariableItemEventTest {

    @Test
    void whenFireEventThenOpenGlobalVariableTab() {
        GlobalVariableSystem system = Mockito.mock(GlobalVariableSystem.class);
        //when
        SystemVariableDTO dto = new SystemVariableDTO();
        EditVariableItemEvent.fireEvent(dto, system);
        //then
        verify(system).notify(GlobalVariableEvent.OPEN_GLOBAL_VARIABLE_TAB, dto);
    }

}