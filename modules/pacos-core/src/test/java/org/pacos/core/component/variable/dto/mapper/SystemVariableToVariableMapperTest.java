package org.pacos.core.component.variable.dto.mapper;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.vaadin.addons.variablefield.data.Variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SystemVariableToVariableMapperTest {


    @Test
    void whenMapSystemVariableDTOThenCorrectVariableIsCreated() {
        SystemVariableDTO systemVariableDTO = new SystemVariableDTO();
        systemVariableDTO.setId(1);
        systemVariableDTO.setName("TestName");
        systemVariableDTO.setDescription("TestDescription");

        Variable variable = SystemVariableToVariableMapper.map(systemVariableDTO);

        assertEquals(1, variable.getId());
        assertEquals("TestName", variable.getName());
        assertNull(variable.getDescription());
        assertNull(variable.getCurrentValue());
    }

    @Test
    void whenMapSystemVariableDTOWithResultThenCorrectVariableIsCreated() {
        SystemVariableDTO systemVariableDTO = new SystemVariableDTO();
        systemVariableDTO.setId(1);
        systemVariableDTO.setName("TestName");
        systemVariableDTO.setDescription("TestDescription");

        Variable variable = SystemVariableToVariableMapper.map(systemVariableDTO, "ResultValue");

        assertEquals(1, variable.getId());
        assertEquals("TestName", variable.getName());
        assertNull(variable.getDescription());
        assertNull(variable.getCurrentValue());
    }
}
