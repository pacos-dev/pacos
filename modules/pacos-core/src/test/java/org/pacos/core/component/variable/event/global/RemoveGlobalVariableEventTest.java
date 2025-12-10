package org.pacos.core.component.variable.event.global;

import com.vaadin.flow.data.selection.SingleSelect;
import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.base.event.UISystem;
import org.pacos.base.window.config.impl.ConfirmationWindowConfig;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.proxy.SystemVariableProxy;
import org.pacos.core.component.variable.system.global.GlobalVariableSystem;
import org.pacos.core.component.variable.view.global.SystemVariableGrid;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RemoveGlobalVariableEventTest {

    private GlobalVariableSystem globalVariableSystem;
    private SingleSelect singleSelect;

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();
        this.globalVariableSystem = Mockito.mock(GlobalVariableSystem.class);
        SystemVariableGrid variableGrid = Mockito.mock(SystemVariableGrid.class);
        this.singleSelect = Mockito.mock(SingleSelect.class);
        when(globalVariableSystem.getVariableGrid()).thenReturn(variableGrid);
        when(globalVariableSystem.getSystemVariableProxy()).thenReturn(mock(SystemVariableProxy.class));
        when(variableGrid.asSingleSelect()).thenReturn(singleSelect);
    }

    @Test
    void whenVariableNotSelectThenDoNotDisplayConfirmationWindow() {
        when(singleSelect.getValue()).thenReturn(null);
        //when
        RemoveGlobalVariableEvent.fireEvent(globalVariableSystem);
        //then
        verifyNoInteractions(UISystem.getCurrent().getWindowManager());
    }

    @Test
    void whenVariableSelectedThenDisplayConfirmationWindow() {
        when(singleSelect.getValue()).thenReturn(new SystemVariableDTO());
        //when
        RemoveGlobalVariableEvent.fireEvent(globalVariableSystem);
        //then
        verify(UISystem.getCurrent().getWindowManager()).showModalWindow(any(ConfirmationWindowConfig.class));
    }

    @Test
    void whenConfirmEventThenRemoveVariable() {
        SystemVariableDTO dto = new SystemVariableDTO();

        RemoveGlobalVariableEvent.onConfirmEvent(globalVariableSystem, dto).confirm();

        verify(globalVariableSystem.getSystemVariableProxy()).removeVariable(dto);
    }
}