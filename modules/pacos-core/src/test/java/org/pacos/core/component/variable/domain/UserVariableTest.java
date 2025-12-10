package org.pacos.core.component.variable.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserVariableTest {

    @Test
    void whenSetIdThenIdIsSet() {
        UserVariable userVariable = new UserVariable();
        userVariable.setId(1);
        assertEquals(1, userVariable.getId());
    }

    @Test
    void whenSetEnabledThenEnabledIsSet() {
        UserVariable userVariable = new UserVariable();
        userVariable.setEnabled(true);
        assertTrue(userVariable.isEnabled());
    }

    @Test
    void whenSetNameThenNameIsSet() {
        UserVariable userVariable = new UserVariable();
        userVariable.setName("TestName");
        assertEquals("TestName", userVariable.getName());
    }

    @Test
    void whenSetCollectionIdThenCollectionIdIsSet() {
        UserVariable userVariable = new UserVariable();
        userVariable.setCollectionId(100);
        assertEquals(100, userVariable.getCollectionId());
    }

    @Test
    void whenSetInitialValueThenInitialValueIsSet() {
        UserVariable userVariable = new UserVariable();
        userVariable.setInitialValue("InitialValue");
        assertEquals("InitialValue", userVariable.getInitialValue());
    }

    @Test
    void whenSetCurrentValueThenCurrentValueIsSet() {
        UserVariable userVariable = new UserVariable();
        userVariable.setCurrentValue("CurrentValue");
        assertEquals("CurrentValue", userVariable.getCurrentValue());
    }

    @Test
    void whenSetCollectionThenCollectionIsSet() {
        UserVariable userVariable = new UserVariable();
        UserVariableCollection collection = new UserVariableCollection();
        userVariable.setCollection(collection);
        assertEquals(collection, userVariable.getCollection());
    }
}
