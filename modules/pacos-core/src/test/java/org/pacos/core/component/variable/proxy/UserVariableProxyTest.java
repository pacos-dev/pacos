package org.pacos.core.component.variable.proxy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.exception.PacosException;
import org.pacos.core.component.variable.domain.UserVariable;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.component.variable.dto.mapper.UserVariableMapper;
import org.pacos.core.component.variable.service.UserVariableCollectionService;
import org.pacos.core.component.variable.service.UserVariableService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserVariableProxyTest {

    private final UserVariableService userVariableServiceMock = mock(UserVariableService.class);
    private final UserVariableCollectionService collectionServiceMock = mock(UserVariableCollectionService.class);
    private final UserVariableProxy userVariableProxy = new UserVariableProxy(userVariableServiceMock, collectionServiceMock);

    @Test
    void whenLoadVariablesThenReturnMappedList() {
        //when
        int collectionId = 1;
        List<UserVariable> userVariables = List.of(mock(UserVariable.class));
        when(userVariableServiceMock.loadForCollection(collectionId)).thenReturn(userVariables);
        try (MockedStatic<UserVariableMapper> mapperMock = mockStatic(UserVariableMapper.class)) {
            mapperMock.when(() -> UserVariableMapper.map(any(UserVariable.class))).thenReturn(mock(UserVariableDTO.class));

            //when
            List<UserVariableDTO> result = userVariableProxy.loadVariables(collectionId);

            //then
            assertNotNull(result);
            assertEquals(userVariables.size(), result.size());
        }
    }

    @Test
    void whenLoadGlobalVariablesThenReturnMappedList() {
        //when
        int userId = 123;
        List<UserVariable> userVariables = List.of(mock(UserVariable.class));
        when(userVariableServiceMock.loadGlobalVariables(userId)).thenReturn(userVariables);
        try (MockedStatic<UserVariableMapper> mapperMock = mockStatic(UserVariableMapper.class)) {
            mapperMock.when(() -> UserVariableMapper.map(any(UserVariable.class))).thenReturn(mock(UserVariableDTO.class));

            //when
            List<UserVariableDTO> result = userVariableProxy.loadGlobalVariables(userId);
            //then
            assertNotNull(result);
            assertEquals(userVariables.size(), result.size());
        }
    }

    @Test
    void whenLoadGlobalVariablesByNameThenReturnOptionalMappedVariable() {
        //when
        int userId = 123;
        String name = "testVariable";
        UserVariable userVariableMock = mock(UserVariable.class);
        when(userVariableServiceMock.loadGlobalVariablesByName(userId, name)).thenReturn(Optional.of(userVariableMock));
        try (MockedStatic<UserVariableMapper> mapperMock = mockStatic(UserVariableMapper.class)) {
            mapperMock.when(() -> UserVariableMapper.map(any(UserVariable.class))).thenReturn(mock(UserVariableDTO.class));

            //when
            Optional<UserVariableDTO> result = userVariableProxy.loadGlobalVariablesByName(userId, name);

            //then
            assertTrue(result.isPresent());
        }
    }

    @Test
    void whenSaveVariableThenReturnUpdatedDTO() {
        //when
        UserVariableDTO variableDTO = mock(UserVariableDTO.class);
        when(userVariableServiceMock.save(variableDTO)).thenReturn(1);
        doNothing().when(variableDTO).setId(1);

        //when
        UserVariableDTO result = userVariableProxy.saveVariable(variableDTO);

        //then
        assertNotNull(result);
        verify(variableDTO).setId(1);
    }

    @Test
    void whenSaveForCollectionThenReturnMappedList() {
        //when
        int collectionId = 1;
        UserVariableDTO variableDTO = mock(UserVariableDTO.class);
        UserVariable userVariableMock = mock(UserVariable.class);
        List<UserVariableDTO> variablesDTO = List.of(variableDTO);
        List<UserVariable> variables = List.of(userVariableMock);
        when(collectionServiceMock.saveVariables(collectionId, variables)).thenReturn(variables);
        try (MockedStatic<UserVariableMapper> mapperMock = mockStatic(UserVariableMapper.class);) {
            mapperMock.when(() -> UserVariableMapper.map(any(UserVariable.class))).thenReturn(mock(UserVariableDTO.class));
            mapperMock.when(() -> UserVariableMapper.bindFromDTO(variableDTO)).thenReturn(userVariableMock);

            //when
            List<UserVariableDTO> result = userVariableProxy.saveForCollection(collectionId, variablesDTO);

            //then
            assertNotNull(result);
            assertEquals(variables.size(), result.size());
        }
    }

    @Test
    void whenSaveForCollectionAndExceptionThenThrowException() {
        //when
        int collectionId = 1;
        UserVariableDTO variableDTO = mock(UserVariableDTO.class);
        UserVariable userVariableMock = mock(UserVariable.class);
        List<UserVariableDTO> variablesDTO = List.of(variableDTO);
        List<UserVariable> variables = List.of(userVariableMock);
        doThrow(RuntimeException.class).when(collectionServiceMock).saveVariables(collectionId, variables);
        try (MockedStatic<UserVariableMapper> mapperMock = mockStatic(UserVariableMapper.class);) {
            mapperMock.when(() -> UserVariableMapper.map(any(UserVariable.class))).thenReturn(mock(UserVariableDTO.class));
            mapperMock.when(() -> UserVariableMapper.bindFromDTO(variableDTO)).thenReturn(userVariableMock);

            //when
            assertThrows(PacosException.class, () -> userVariableProxy.saveForCollection(collectionId, variablesDTO));
        }
    }

    @Test
    void whenLoadVariableThenReturnOptionalMappedVariable() {
        //when
        int collectionId = 1;
        String variableName = "testVariable";
        UserVariable userVariableMock = mock(UserVariable.class);
        when(userVariableServiceMock.findByCollectionIdAndName(collectionId, variableName)).thenReturn(Optional.of(userVariableMock));
        try (MockedStatic<UserVariableMapper> mapperMock = mockStatic(UserVariableMapper.class)) {
            mapperMock.when(() -> UserVariableMapper.map(any(UserVariable.class))).thenReturn(mock(UserVariableDTO.class));

            //when
            Optional<UserVariableDTO> result = userVariableProxy.loadVariable(collectionId, variableName);

            //then
            assertTrue(result.isPresent());
        }
    }
}
