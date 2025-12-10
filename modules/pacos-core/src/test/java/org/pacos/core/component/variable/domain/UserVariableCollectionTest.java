package org.pacos.core.component.variable.domain;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserVariableCollectionTest {

    @Test
    void whenSetIdThenIdIsSet() {
        UserVariableCollection collection = new UserVariableCollection();
        collection.setId(1);
        assertEquals(1, collection.getId());
    }

    @Test
    void whenSetUserIdThenUserIdIsSet() {
        UserVariableCollection collection = new UserVariableCollection();
        collection.setUserId(100);
        assertEquals(100, collection.getUserId());
    }

    @Test
    void whenSetNameThenNameIsSet() {
        UserVariableCollection collection = new UserVariableCollection();
        collection.setName("TestCollection");
        assertEquals("TestCollection", collection.getName());
    }

    @Test
    void whenSetGlobalThenGlobalIsSet() {
        UserVariableCollection collection = new UserVariableCollection();
        collection.setGlobal(true);
        assertTrue(collection.isGlobal());
    }

    @Test
    void whenSetVariablesThenVariablesAreSet() {
        UserVariableCollection collection = new UserVariableCollection();
        List<UserVariable> variables = new ArrayList<>();
        UserVariable variable = new UserVariable();
        variables.add(variable);
        collection.setVariables(variables);
        assertEquals(variables, collection.getVariables());
    }

    @Test
    void whenAddVariableToVariablesThenVariableIsAdded() {
        UserVariableCollection collection = new UserVariableCollection();
        UserVariable variable = new UserVariable();
        collection.getVariables().add(variable);
        assertTrue(collection.getVariables().contains(variable));
    }

    @Test
    void whenRemoveVariableFromVariablesThenVariableIsRemoved() {
        UserVariableCollection collection = new UserVariableCollection();
        UserVariable variable = new UserVariable();
        collection.getVariables().add(variable);
        collection.getVariables().remove(variable);
        assertFalse(collection.getVariables().contains(variable));
    }
}
