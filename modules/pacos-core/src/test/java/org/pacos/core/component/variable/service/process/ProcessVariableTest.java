package org.pacos.core.component.variable.service.process;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.config.PluginManagerMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.base.session.UserDTO;
import org.pacos.base.variable.ScopeDefinition;
import org.pacos.core.component.variable.service.ProcessVariable;
import org.pacos.core.component.variable.service.provider.SystemVariableProviderImpl;
import org.vaadin.addons.variablefield.data.Scope;
import org.vaadin.addons.variablefield.data.ScopeName;
import org.vaadin.addons.variablefield.data.Variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class ProcessVariableTest {

    private SystemVariableProviderImpl variableProvider;
    private ProcessVariable processVariable;
    private final UserDTO userDTO = new UserDTO(1, "admin", 1);

    @BeforeEach
    void init() {
        this.variableProvider = Mockito.mock(SystemVariableProviderImpl.class);
        this.processVariable = new ProcessVariable();
        PluginManagerMock.mockVariableProvider(Map.of("SystemVariableProviderImpl", variableProvider));
    }

    @Test
    void whenProcessLineWithoutVariablesThenDoNotReplaceAnything() {
        //given
        String line = "Line without any variable ;)";
        //when
        String result = new ProcessVariable().process((Collections.singletonList(ScopeDefinition.SYSTEM)), line, userDTO);
        //then
        assertEquals("Line without any variable ;)", result);
    }

    @Test
    void whenProcessLineWithNotDefinedVariableThenDoNotReplaceAnything() {
        //given
        String line = "Line without any variable  {{$variable}} ;)";
        //when
        String result = processVariable.process((Collections.singletonList(ScopeDefinition.SYSTEM)), line, userDTO);
        //then
        assertEquals("Line without any variable  {{$variable}} ;)", result);
    }


    @Test
    void whenGlobalVariableThenReplaceWithCurrentValue() {
        //given
        when(variableProvider.loadVariable(ScopeDefinition.SYSTEM, "$globalVar")).
                thenReturn(Optional.of(
                        new Variable(1, "$globalVar", "test", "test2", "des")));
        when(variableProvider.isScopeSupported(ScopeDefinition.SYSTEM_NAME)).thenReturn(true);
        String line = "Line with variable {{$globalVar}} ;)";
        //when
        String result = processVariable.process((Collections.singletonList(ScopeDefinition.SYSTEM)), line, userDTO);
        //then
        assertEquals("Line with variable test2 ;)", result);
    }

    @Test
    void whenCurrentValueIsNotSetThenUseInitialValue() {
        //given
        when(variableProvider.loadVariable(ScopeDefinition.SYSTEM, "$globalVar")).
                thenReturn(Optional.of(
                        new Variable(1, "$globalVar", "test", "", "des")));
        when(variableProvider.isScopeSupported(ScopeDefinition.SYSTEM_NAME)).thenReturn(true);
        String line = "Line with variable {{$globalVar}} ;)";
        //when
        String result = processVariable.process((Collections.singletonList(ScopeDefinition.SYSTEM)), line, userDTO);
        //then
        assertEquals("Line with variable test ;)", result);
    }

    @Test
    void whenCurrentValueIsNullThenUseInitialValue() {
        //given
        when(variableProvider.loadVariable(ScopeDefinition.SYSTEM, "$globalVar")).
                thenReturn(Optional.of(
                        new Variable(1, "$globalVar", "test", null, "des")));
        when(variableProvider.isScopeSupported(ScopeDefinition.SYSTEM_NAME)).thenReturn(true);
        String line = "Line with variable {{$globalVar}} ;)";
        //when
        String result = processVariable.process((Collections.singletonList(ScopeDefinition.SYSTEM)), line, userDTO);
        //then
        assertEquals("Line with variable test ;)", result);
    }

    @Test
    void whenCurrentValueAndInitialValueAreEmptyThenLeaveVariableName() {
        //given
        when(variableProvider.loadVariable(ScopeDefinition.SYSTEM, "$globalVar")).
                thenReturn(Optional.of(
                        new Variable(1, "$globalVar", null, null, "des")));
        when(variableProvider.isScopeSupported(ScopeDefinition.SYSTEM_NAME)).thenReturn(true);
        String line = "Line with variable {{$globalVar}} ;)";
        //when
        String result = processVariable.process((Collections.singletonList(ScopeDefinition.SYSTEM)), line, userDTO);
        //then
        assertEquals("Line with variable {{$globalVar}} ;)", result);
    }

    @Test
    void whenVariableIsSetThenReplaceAllOccurrence() {
        //given
        when(variableProvider.loadVariable(ScopeDefinition.SYSTEM, "$globalVar")).
                thenReturn(Optional.of(
                        new Variable(1, "$globalVar", "init", "var", "des")));
        when(variableProvider.isScopeSupported(ScopeDefinition.SYSTEM_NAME)).thenReturn(true);
        String line = "Line with variable {{$globalVar}} {{$globalVar}} {{$globalVar}} ;)";
        //when
        String result = processVariable.process((Collections.singletonList(ScopeDefinition.SYSTEM)), line, userDTO);
        //then
        assertEquals("Line with variable var var var ;)", result);
    }

    @Test
    void whenMoreThenOneProviderConfiguredThenUseVariableFromFirstDefinedOnScopeList() {
        //given
        SystemVariableProviderImpl variableProvider2 = Mockito.mock(SystemVariableProviderImpl.class);

        PluginManagerMock.mockVariableProvider(Map.of("SystemVariableProviderImpl", variableProvider,
                "SystemVariableProviderImpl2", variableProvider2));
        Scope scope2 = new Scope("system2", 1, 'G', "#0262f3");

        when(variableProvider.loadVariable(ScopeDefinition.SYSTEM, "$globalVar")).
                thenReturn(Optional.of(
                        new Variable(1, "$globalVar", "init", "init", "des")));
        when(variableProvider2.loadVariable(scope2, "$globalVar")).
                thenReturn(Optional.of(
                        new Variable(1, "$globalVar", "var2", "var2", "des")));
        when(variableProvider.isScopeSupported(ScopeDefinition.SYSTEM_NAME)).thenReturn(true);
        when(variableProvider2.isScopeSupported(new ScopeName("system2"))).thenReturn(true);
        String line = "Line with variable {{$globalVar}} {{$globalVar}} {{$globalVar}} ;)";
        //when
        String result = processVariable.process((List.of(ScopeDefinition.SYSTEM, scope2)), line, userDTO);
        //then
        assertEquals("Line with variable init init init ;)", result);
    }

    @Test
    void whenLineIsNullThenReturnNull() {
        PluginManagerMock.mockVariableProvider(Map.of("SystemVariableProviderImpl", variableProvider));
        //when
        assertNull(processVariable.process((Collections.singletonList(ScopeDefinition.SYSTEM)), null, userDTO));
    }

}