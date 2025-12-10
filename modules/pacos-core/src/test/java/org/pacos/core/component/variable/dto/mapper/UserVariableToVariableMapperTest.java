package org.pacos.core.component.variable.dto.mapper;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.vaadin.addons.variablefield.data.Variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserVariableToVariableMapperTest {

    @Test
    void whenMapUserVariableDTOThenCorrectVariableIsCreated() {
        UserVariableDTO dto = new UserVariableDTO();
        dto.setId(1);
        dto.setName("TestName");
        dto.setInitialValue("InitialValue");
        dto.setCurrentValue("CurrentValue");

        Variable variable = UserVariableToVariableMapper.map(dto);

        assertEquals(1, variable.getId());
        assertEquals("TestName", variable.getName());
        assertEquals("InitialValue", variable.getInitialValue());
        assertEquals("CurrentValue", variable.getCurrentValue());
        assertNull(variable.getDescription());
    }
}
