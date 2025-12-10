package org.pacos.core.component.variable.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SystemVariableDTOTest {

    @Test
    void whenSetIdThenIdIsSet() {
        SystemVariableDTO dto = new SystemVariableDTO();
        dto.setId(1);
        assertEquals(1, dto.getId());
    }

    @Test
    void whenSetNameThenNameIsSet() {
        SystemVariableDTO dto = new SystemVariableDTO();
        dto.setName("TestName");
        assertEquals("TestName", dto.getName());
    }

    @Test
    void whenSetDescriptionThenDescriptionIsSet() {
        SystemVariableDTO dto = new SystemVariableDTO();
        dto.setDescription("TestDescription");
        assertEquals("TestDescription", dto.getDescription());
    }

    @Test
    void whenSetJsThenJsIsSet() {
        SystemVariableDTO dto = new SystemVariableDTO();
        dto.setJs("TestJs");
        assertEquals("TestJs", dto.getJs());
    }

    @Test
    void whenTwoEqualObjectsThenEqualsReturnsTrue() {
        SystemVariableDTO dto1 = new SystemVariableDTO();
        dto1.setId(1);
        SystemVariableDTO dto2 = new SystemVariableDTO();
        dto2.setId(1);
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void whenTwoDifferentObjectsThenEqualsReturnsFalse() {
        SystemVariableDTO dto1 = new SystemVariableDTO();
        dto1.setId(1);
        SystemVariableDTO dto2 = new SystemVariableDTO();
        dto2.setId(2);
        assertNotEquals(dto1, dto2);
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

}
