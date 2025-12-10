package org.pacos.core.component.variable.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserVariableDTOTest {

    @Test
    void whenSetIdThenIdIsSet() {
        UserVariableDTO userVariableDTO = new UserVariableDTO();
        userVariableDTO.setId(1);
        assertEquals(1, userVariableDTO.getId());
    }

    @Test
    void whenSetEnabledThenEnabledIsSet() {
        UserVariableDTO userVariableDTO = new UserVariableDTO();
        userVariableDTO.setEnabled(true);
        assertTrue(userVariableDTO.isEnabled());
    }

    @Test
    void whenSetNameThenNameIsSet() {
        UserVariableDTO userVariableDTO = new UserVariableDTO();
        userVariableDTO.setName("TestName");
        assertEquals("TestName", userVariableDTO.getName());
    }

    @Test
    void whenSetInitialValueThenInitialValueIsSet() {
        UserVariableDTO userVariableDTO = new UserVariableDTO();
        userVariableDTO.setInitialValue("InitialValue");
        assertEquals("InitialValue", userVariableDTO.getInitialValue());
    }

    @Test
    void whenSetCurrentValueThenCurrentValueIsSet() {
        UserVariableDTO userVariableDTO = new UserVariableDTO();
        userVariableDTO.setCurrentValue("CurrentValue");
        assertEquals("CurrentValue", userVariableDTO.getCurrentValue());
    }

    @Test
    void whenSetCollectionIdThenCollectionIdIsSet() {
        UserVariableDTO userVariableDTO = new UserVariableDTO();
        userVariableDTO.setCollectionId(100);
        assertEquals(100, userVariableDTO.getCollectionId());
    }


    @Test
    void whenTwoEqualObjectsThenEqualsReturnsTrue() {
        UserVariableDTO userVariableDTO1 = new UserVariableDTO();
        userVariableDTO1.setName("test");
        userVariableDTO1.setCollectionId(1);
        UserVariableDTO userVariableDTO2 = new UserVariableDTO();
        userVariableDTO2.setName("test");
        userVariableDTO2.setCollectionId(1);
        assertEquals(userVariableDTO1, userVariableDTO2);
    }

    @Test
    void whenTwoDifferentObjectsThenEqualsReturnsFalse() {
        UserVariableDTO userVariableDTO1 = new UserVariableDTO();
        userVariableDTO1.setName("test");
        userVariableDTO1.setCollectionId(1);
        UserVariableDTO userVariableDTO2 = new UserVariableDTO();
        userVariableDTO2.setName("test2");
        userVariableDTO2.setCollectionId(1);
        assertNotEquals(userVariableDTO1, userVariableDTO2);
    }

    @Test
    void whenCallHashCodeThenCorrectHashCodeIsGenerated() {
        UserVariableDTO userVariableDTO = new UserVariableDTO();
        userVariableDTO.setName("test");
        userVariableDTO.setCollectionId(1);
        UserVariableDTO userVariableDTO2 = new UserVariableDTO();
        userVariableDTO2.setName("test");
        userVariableDTO2.setCollectionId(1);
        assertEquals(userVariableDTO.hashCode(), userVariableDTO2.hashCode());
    }
}
