package org.pacos.core.component.variable.dto.mapper;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.domain.SystemVariable;
import org.pacos.core.component.variable.dto.SystemVariableDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemVariableMapperTest {

    @Test
    void whenMapSystemVariableThenCorrectDTOIsCreated() {
        SystemVariable variable = new SystemVariable();
        variable.setId(1);
        variable.setName("TestName");
        variable.setDescription("TestDescription");
        variable.setJs("TestJs");

        SystemVariableDTO dto = SystemVariableMapper.map(variable);

        assertEquals(1, dto.getId());
        assertEquals("TestName", dto.getName());
        assertEquals("TestDescription", dto.getDescription());
        assertEquals("TestJs", dto.getJs());
    }
}
