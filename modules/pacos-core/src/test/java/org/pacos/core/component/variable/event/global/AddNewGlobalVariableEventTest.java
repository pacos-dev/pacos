package org.pacos.core.component.variable.event.global;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.proxy.SystemVariableProxy;
import org.pacos.core.component.variable.system.global.GlobalVariableEvent;
import org.pacos.core.component.variable.system.global.GlobalVariableSystem;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class AddNewGlobalVariableEventTest {

    @Test
    void whenTriggerEventThenInitNewEmptyVariable() {
        GlobalVariableSystem system = Mockito.mock(GlobalVariableSystem.class);
        SystemVariableProxy systemVariableProxy = Mockito.mock(SystemVariableProxy.class);
        when(system.getSystemVariableProxy()).thenReturn(systemVariableProxy);
        //when
        AddNewGlobalVariableEvent.fireEvent(system);
        //then
        verify(systemVariableProxy).initNewVariable();
    }

    @Test
    void whenTriggerEventThenAddVariableToGrid() {
        GlobalVariableSystem system = Mockito.mock(GlobalVariableSystem.class);
        SystemVariableProxy systemVariableProxy = Mockito.mock(SystemVariableProxy.class);
        when(system.getSystemVariableProxy()).thenReturn(systemVariableProxy);
        SystemVariableDTO variable = new SystemVariableDTO();
        when(systemVariableProxy.initNewVariable()).thenReturn(variable);
        //when
        AddNewGlobalVariableEvent.fireEvent(system);
        //then
        verify(system).notify(GlobalVariableEvent.ADD_NEW_VARIABLE_TO_GRID, variable);
    }

    @Test
    void whenTriggerEventThenOpenNewTabInVariableGrid() {
        GlobalVariableSystem system = Mockito.mock(GlobalVariableSystem.class);
        SystemVariableProxy systemVariableProxy = Mockito.mock(SystemVariableProxy.class);
        when(system.getSystemVariableProxy()).thenReturn(systemVariableProxy);
        SystemVariableDTO variable = new SystemVariableDTO();
        when(systemVariableProxy.initNewVariable()).thenReturn(variable);
        //when
        AddNewGlobalVariableEvent.fireEvent(system);
        //then
        verify(system).notify(GlobalVariableEvent.OPEN_GLOBAL_VARIABLE_TAB, variable);
    }

}