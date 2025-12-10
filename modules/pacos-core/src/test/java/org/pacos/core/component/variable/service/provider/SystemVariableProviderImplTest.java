package org.pacos.core.component.variable.service.provider;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.variable.ScopeDefinition;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.dto.mapper.SystemVariableToVariableMapper;
import org.pacos.core.component.variable.generator.DynamicJavaScriptRunner;
import org.pacos.core.component.variable.proxy.SystemVariableProxy;
import org.vaadin.addons.variablefield.data.Variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class SystemVariableProviderImplTest {

    private final SystemVariableProxy systemVariableProxyMock = mock(SystemVariableProxy.class);
    private final SystemVariableProviderImpl systemVariableProvider = new SystemVariableProviderImpl(systemVariableProxyMock);

    @Test
    void whenLoadVariablesThenReturnSystemVariables() {
        //when
        Variable variable = mock(Variable.class);
        when(systemVariableProxyMock.loadSystemVariable()).thenReturn(List.of(variable));

        //when
        List<Variable> result = systemVariableProvider.loadVariables(ScopeDefinition.SYSTEM);

        //then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void whenIsScopeSupportedThenReturnTrue() {
        assertTrue(systemVariableProvider.isScopeSupported(ScopeDefinition.SYSTEM_NAME));
    }

    @Test
    void whenLoadVariableAndVariableNotFoundThenReturnEmptyOptional() {
        //when
        String variableName = "TestVariable";
        when(systemVariableProxyMock.findVariable(variableName)).thenReturn(Optional.empty());

        //when
        Optional<Variable> result = systemVariableProvider.loadVariable(ScopeDefinition.SYSTEM, variableName);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void whenLoadVariableAndJsIsNullThenReturnEmptyOptional() {
        //when
        String variableName = "TestVariable";
        SystemVariableDTO variableDTO = mock(SystemVariableDTO.class);
        when(variableDTO.getJs()).thenReturn(null);
        when(systemVariableProxyMock.findVariable(variableName)).thenReturn(Optional.of(variableDTO));

        //when
        Optional<Variable> result = systemVariableProvider.loadVariable(ScopeDefinition.SYSTEM, variableName);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void whenLoadVariableThenReturnMappedVariableWithExecutedJs() {
        //when
        String variableName = "TestVariable";
        String jsCode = "return 42;";
        String jsResult = "42";

        SystemVariableDTO variableDTO = mock(SystemVariableDTO.class);
        when(variableDTO.getJs()).thenReturn(jsCode);
        when(systemVariableProxyMock.findVariable(variableName)).thenReturn(Optional.of(variableDTO));
        Variable mappedVariable = mock(Variable.class);

        try (MockedStatic<SystemVariableToVariableMapper> mapper = mockStatic(SystemVariableToVariableMapper.class);
             MockedStatic<DynamicJavaScriptRunner> scriptRunner = mockStatic(DynamicJavaScriptRunner.class)
        ) {
            mapper.when(() -> DynamicJavaScriptRunner.runCode(jsCode)).thenReturn(jsResult);
            scriptRunner.when(() -> SystemVariableToVariableMapper.map(variableDTO, jsResult)).thenReturn(mappedVariable);
            //when
            Optional<Variable> result = systemVariableProvider.loadVariable(ScopeDefinition.SYSTEM, variableName);

            //then
            assertTrue(result.isPresent());
            assertEquals(mappedVariable, result.get());
        }
    }
}
