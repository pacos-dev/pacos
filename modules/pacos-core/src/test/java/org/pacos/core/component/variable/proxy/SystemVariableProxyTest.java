package org.pacos.core.component.variable.proxy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.domain.SystemVariable;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.service.SystemVariableService;
import org.vaadin.addons.variablefield.data.Variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemVariableProxyTest {

    private final SystemVariableService systemVariableServiceMock = mock(SystemVariableService.class);
    private final SystemVariableProxy systemVariableProxy = new SystemVariableProxy(systemVariableServiceMock);

    @Test
    void whenLoadVariablesThenReturnMappedList() {
        //when
        SystemVariable systemVariable = mock(SystemVariable.class);
        when(systemVariableServiceMock.loadAllVariables()).thenReturn(List.of(systemVariable));

        //when
        List<SystemVariableDTO> result = systemVariableProxy.loadVariables();

        //then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void whenInitNewVariableThenReturnMappedVariable() {
        //when
        SystemVariable systemVariable = mock(SystemVariable.class);
        when(systemVariableServiceMock.initNewVariable()).thenReturn(systemVariable);

        //when
        SystemVariableDTO result = systemVariableProxy.initNewVariable();

        //then
        assertNotNull(result);
    }

    @Test
    void whenSaveVariableThenInvokeServiceSave() {
        //when
        SystemVariableDTO dto = mock(SystemVariableDTO.class);

        //when
        systemVariableProxy.save(dto);

        //then
        verify(systemVariableServiceMock).save(dto);
    }

    @Test
    void whenIsUniqueThenReturnServiceResult() {
        //when
        Integer id = 1;
        String name = "VariableName";
        when(systemVariableServiceMock.isUnique(id, name)).thenReturn(true);

        //when
        boolean result = systemVariableProxy.isUnique(id, name);

        //then
        assertTrue(result);
        verify(systemVariableServiceMock).isUnique(id, name);
    }

    @Test
    void whenRemoveVariableThenInvokeServiceRemove() {
        //when
        SystemVariableDTO dto = mock(SystemVariableDTO.class);

        //when
        systemVariableProxy.removeVariable(dto);

        //then
        verify(systemVariableServiceMock).remove(dto);
    }

    @Test
    void whenLoadSystemVariableThenReturnMappedList() {
        //when
        SystemVariable systemVariable = mock(SystemVariable.class);
        when(systemVariableServiceMock.loadAllVariables()).thenReturn(List.of(systemVariable));

        //when
        List<Variable> result = systemVariableProxy.loadSystemVariable();

        //then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void whenFindVariableThenReturnMappedOptional() {
        //when
        String variableName = "TestVariable";
        SystemVariable systemVariable = mock(SystemVariable.class);
        when(systemVariableServiceMock.findByName(variableName)).thenReturn(Optional.of(systemVariable));

        //when
        Optional<SystemVariableDTO> result = systemVariableProxy.findVariable(variableName);

        //then
        assertTrue(result.isPresent());
    }
}
