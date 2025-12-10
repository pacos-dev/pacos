package org.pacos.core.component.variable.dto;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserVariableCollectionDTOTest {

    @Test
    void whenSetIdThenIdIsSet() {
        UserVariableCollectionDTO dto = UserVariableCollectionDTO.builder().id(1).build();
        assertEquals(1, dto.getId());
    }

    @Test
    void whenSetNameThenNameIsSet() {
        UserVariableCollectionDTO dto = UserVariableCollectionDTO.builder().name("TestName").build();
        assertEquals("TestName", dto.getName());
    }

    @Test
    void whenSetUserIdThenUserIdIsSet() {
        UserVariableCollectionDTO dto = UserVariableCollectionDTO.builder().userId(100).build();
        assertEquals(100, dto.getUserId());
    }

    @Test
    void whenSetGlobalThenGlobalIsSet() {
        UserVariableCollectionDTO dto = UserVariableCollectionDTO.builder().global(true).build();
        assertTrue(dto.isGlobal());
    }

    @Test
    void whenSetVariablesThenVariablesAreSet() {
        List<UserVariableDTO> variables = new ArrayList<>();
        UserVariableDTO variable = new UserVariableDTO();
        variables.add(variable);
        UserVariableCollectionDTO dto = UserVariableCollectionDTO.builder().variables(variables).build();
        assertEquals(variables, dto.getVariables());
    }

    @Test
    void whenGetDisplayNameThenCorrectNameIsReturned() {
        UserVariableCollectionDTO dto = UserVariableCollectionDTO.builder().name("TestName").build();
        assertEquals("TestName", dto.getDisplayName());
    }

    @Test
    void whenTwoEqualObjectsThenEqualsReturnsTrue() {
        UserVariableCollectionDTO dto1 = UserVariableCollectionDTO.builder().id(1).name("Test").build();
        UserVariableCollectionDTO dto2 = UserVariableCollectionDTO.builder().id(1).name("Different").build();
        assertEquals(dto1, dto2);
    }

    @Test
    void whenTwoDifferentObjectsThenEqualsReturnsFalse() {
        UserVariableCollectionDTO dto1 = UserVariableCollectionDTO.builder().id(1).build();
        UserVariableCollectionDTO dto2 = UserVariableCollectionDTO.builder().id(2).build();
        assertNotEquals(dto1, dto2);
    }

    @Test
    void whenCallHashCodeThenCorrectHashCodeIsGenerated() {
        UserVariableCollectionDTO dto = UserVariableCollectionDTO.builder().id(1).build();
        UserVariableCollectionDTO dto2 = UserVariableCollectionDTO.builder().id(1).build();
        assertEquals(dto.hashCode(), dto2.hashCode());
    }
}
