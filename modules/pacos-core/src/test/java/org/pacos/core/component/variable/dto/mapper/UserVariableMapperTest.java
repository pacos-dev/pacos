package org.pacos.core.component.variable.dto.mapper;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.domain.UserVariable;
import org.pacos.core.component.variable.dto.UserVariableDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserVariableMapperTest {

    @Test
    void whenBindFromDTOToExistingThenExistingObjectIsUpdated() {
        UserVariableDTO dto = new UserVariableDTO();
        dto.setId(1);
        dto.setName("TestName");
        dto.setInitialValue("InitialValue");
        dto.setCurrentValue("CurrentValue");
        dto.setEnabled(true);
        dto.setCollectionId(100);

        UserVariable existing = new UserVariable();

        UserVariable result = UserVariableMapper.bindFromDTO(dto, existing);

        assertEquals(1, result.getId());
        assertEquals("TestName", result.getName());
        assertEquals("InitialValue", result.getInitialValue());
        assertEquals("CurrentValue", result.getCurrentValue());
        assertTrue(result.isEnabled());
        assertEquals(100, result.getCollectionId());
    }

    @Test
    void whenBindFromDTOThenNewObjectIsCreated() {
        UserVariableDTO dto = new UserVariableDTO();
        dto.setId(1);
        dto.setName("TestName");
        dto.setInitialValue("InitialValue");
        dto.setCurrentValue("CurrentValue");
        dto.setEnabled(true);
        dto.setCollectionId(100);

        UserVariable result = UserVariableMapper.bindFromDTO(dto);

        assertEquals(1, result.getId());
        assertEquals("TestName", result.getName());
        assertEquals("InitialValue", result.getInitialValue());
        assertEquals("CurrentValue", result.getCurrentValue());
        assertTrue(result.isEnabled());
        assertEquals(100, result.getCollectionId());
    }

    @Test
    void whenMapVariableToDTOThenCorrectDTOIsCreated() {
        UserVariable variable = new UserVariable();
        variable.setId(1);
        variable.setName("TestName");
        variable.setInitialValue("InitialValue");
        variable.setCurrentValue("CurrentValue");
        variable.setEnabled(true);
        variable.setCollectionId(100);

        UserVariableDTO dto = UserVariableMapper.map(variable);

        assertEquals(1, dto.getId());
        assertEquals("TestName", dto.getName());
        assertEquals("InitialValue", dto.getInitialValue());
        assertEquals("CurrentValue", dto.getCurrentValue());
        assertTrue(dto.isEnabled());
        assertEquals(100, dto.getCollectionId());
    }
}
