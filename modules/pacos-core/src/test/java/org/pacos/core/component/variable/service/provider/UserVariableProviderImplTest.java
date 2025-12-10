package org.pacos.core.component.variable.service.provider;

import java.util.List;
import java.util.Optional;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.base.variable.ScopeDefinition;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.component.variable.dto.mapper.UserVariableToVariableMapper;
import org.pacos.core.component.variable.proxy.UserVariableProxy;
import org.vaadin.addons.variablefield.data.Scope;
import org.vaadin.addons.variablefield.data.Variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class UserVariableProviderImplTest {

    private final UserVariableProxy userVariableProxyMock = mock(UserVariableProxy.class);
    private final UserVariableProviderImpl provider = new UserVariableProviderImpl(userVariableProxyMock);

    @Test
    void whenLoadVariablesAndUserSessionNullThenReturnEmptyList() {
        VaadinMock.mockVaadinSession();

        List<Variable> result = provider.loadVariables(mock(Scope.class));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenLoadVariablesThenReturnMappedVariables() {
        VaadinMock.mockSystem();

        UserVariableDTO enabledVariable = mock(UserVariableDTO.class);
        when(enabledVariable.isEnabled()).thenReturn(true);

        UserVariableDTO disabledVariable = mock(UserVariableDTO.class);
        when(disabledVariable.isEnabled()).thenReturn(false);

        when(userVariableProxyMock.loadVariables(0)).thenReturn(List.of(enabledVariable, disabledVariable));

        Variable mappedVariable = mock(Variable.class);
        try (MockedStatic<UserVariableToVariableMapper> mock = mockStatic(UserVariableToVariableMapper.class)) {
            mock.when(() -> UserVariableToVariableMapper.map(enabledVariable)).thenReturn(mappedVariable);
            List<Variable> result = provider.loadVariables(mock(Scope.class));

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(mappedVariable, result.get(0));
        }

    }

    @Test
    void whenIsScopeSupportedThenReturnTrue() {
        assertTrue(provider.isScopeSupported(ScopeDefinition.USER_NAME));
    }

    @Test
    void whenLoadVariableAndUserSessionNullThenReturnEmptyOptional() {
        VaadinMock.mockVaadinSession();

        Optional<Variable> result = provider.loadVariable(mock(Scope.class), "variableName");

        assertTrue(result.isEmpty());
    }

    @Test
    void whenLoadVariableThenReturnMappedVariable() {
        VaadinMock.mockSystem();
        UserVariableDTO variableDTO = mock(UserVariableDTO.class);
        when(userVariableProxyMock.loadVariable(0, "variableName")).thenReturn(Optional.of(variableDTO));

        Variable mappedVariable = mock(Variable.class);
        try (MockedStatic<UserVariableToVariableMapper> mock = mockStatic(UserVariableToVariableMapper.class)) {
            mock.when(() -> UserVariableToVariableMapper.map(variableDTO)).thenReturn(mappedVariable);

            Optional<Variable> result = provider.loadVariable(mock(Scope.class), "variableName");

            assertTrue(result.isPresent());
            assertEquals(mappedVariable, result.get());
        }
    }

    @Test
    void whenLoadVariableAndCollectionNotPresentThenReturnEmptyOptional() {
        VaadinMock.mockSystem();

        Optional<Variable> result = provider.loadVariable(mock(Scope.class), "variableName");

        assertTrue(result.isEmpty());
    }

    @Test
    void whenLoadVariableWithNameAndUserDTOThenReturnMappedVariable() {

        VaadinMock.mockSystem();
        UserVariableDTO variableDTO = mock(UserVariableDTO.class);
        when(variableDTO.isEnabled()).thenReturn(true);
        when(userVariableProxyMock.loadVariable(0, "variableName")).thenReturn(Optional.of(variableDTO));

        Variable mappedVariable = mock(Variable.class);
        try (MockedStatic<UserVariableToVariableMapper> mock = mockStatic(UserVariableToVariableMapper.class)) {
            mock.when(() -> UserVariableToVariableMapper.map(variableDTO)).thenReturn(mappedVariable);

            Optional<Variable> result = provider.loadVariable("variableName", UserSession.getCurrent().getUser());

            assertTrue(result.isPresent());
            assertEquals(mappedVariable, result.get());
        }
    }

    @Test
    void whenLoadVariableWithNameAndDisabledThenReturnEmptyOptional() {
        UserDTO userDTO = mock(UserDTO.class);

        UserVariableDTO variableDTO = mock(UserVariableDTO.class);
        when(variableDTO.isEnabled()).thenReturn(false);
        when(userVariableProxyMock.loadVariable(0, "variableName")).thenReturn(Optional.of(variableDTO));

        Optional<Variable> result = provider.loadVariable("variableName", userDTO);

        assertTrue(result.isEmpty());
    }
}
