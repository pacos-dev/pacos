package org.pacos.core.component.variable.service.provider;

import java.util.List;
import java.util.Optional;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.base.variable.ScopeDefinition;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.component.variable.proxy.UserVariableProxy;
import org.vaadin.addons.variablefield.data.Scope;
import org.vaadin.addons.variablefield.data.ScopeName;
import org.vaadin.addons.variablefield.data.Variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserGlobalVariableProviderImplTest {

    private final UserVariableProxy userVariableProxyMock = mock(UserVariableProxy.class);
    private final UserGlobalVariableProviderImpl provider = new UserGlobalVariableProviderImpl(userVariableProxyMock);

    @Test
    void whenLoadVariablesAndUserSessionNullThenReturnEmptyList() {
        VaadinMock.mockVaadinSession();
        //when
        List<Variable> result = provider.loadVariables(mock(Scope.class));

        //then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenLoadVariablesThenReturnEnabledVariables() {
        //when
        VaadinMock.mockSystem();

        UserVariableDTO enabledVariable = mock(UserVariableDTO.class);
        when(enabledVariable.isEnabled()).thenReturn(true);
        UserVariableDTO disabledVariable = mock(UserVariableDTO.class);
        when(disabledVariable.isEnabled()).thenReturn(false);

        when(userVariableProxyMock.loadGlobalVariables(2)).thenReturn(List.of(enabledVariable, disabledVariable));


        //when
        List<Variable> result = provider.loadVariables(mock(Scope.class));

        //then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void whenScopeIsSupportedThenReturnTrue() {
        //when
        boolean result = provider.isScopeSupported(ScopeDefinition.GLOBAL_NAME);
        //then
        assertTrue(result);
    }

    @Test
    void whenScopeIsNotSupportedThenReturnFalse() {
        //when
        boolean result = provider.isScopeSupported(new ScopeName("test"));
        //then
        assertFalse(result);
    }

    @Test
    void whenLoadVariableAndUserSessionNullThenReturnEmptyOptional() {
        VaadinMock.mockVaadinSession();
        //when
        Optional<Variable> result = provider.loadVariable(mock(Scope.class), "variableName");

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void whenLoadVariableThenReturnMappedVariable() {
        //when
        VaadinMock.mockSystem();

        UserVariableDTO variableDTO = mock(UserVariableDTO.class);
        when(variableDTO.isEnabled()).thenReturn(true);
        when(userVariableProxyMock.loadGlobalVariablesByName(2, "variableName")).thenReturn(Optional.of(variableDTO));

        //when
        Optional<Variable> result = provider.loadVariable(mock(Scope.class), "variableName");

        //then
        assertTrue(result.isPresent());
    }

    @Test
    void whenLoadVariableAndVariableNotEnabledThenReturnEmptyOptional() {
        //when
        VaadinMock.mockSystem();

        UserVariableDTO variableDTO = mock(UserVariableDTO.class);
        when(variableDTO.isEnabled()).thenReturn(false);
        when(userVariableProxyMock.loadGlobalVariablesByName(2, "variableName")).thenReturn(Optional.of(variableDTO));

        //when
        Optional<Variable> result = provider.loadVariable(mock(Scope.class), "variableName");

        //then
        assertTrue(result.isEmpty());
    }
}
