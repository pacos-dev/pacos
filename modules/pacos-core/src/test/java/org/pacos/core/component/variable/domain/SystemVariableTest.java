package org.pacos.core.component.variable.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemVariableTest {

    @Test
    void whenSetIdThenIdIsSet() {
        SystemVariable systemVariable = new SystemVariable();
        systemVariable.setId(1);
        assertEquals(1, systemVariable.getId());
    }

    @Test
    void whenSetNameThenNameIsSet() {
        SystemVariable systemVariable = new SystemVariable();
        systemVariable.setName("TestName");
        assertEquals("TestName", systemVariable.getName());
    }

    @Test
    void whenSetDescriptionThenDescriptionIsSet() {
        SystemVariable systemVariable = new SystemVariable();
        systemVariable.setDescription("Test Description");
        assertEquals("Test Description", systemVariable.getDescription());
    }

    @Test
    void whenSetJsThenJsIsSet() {
        SystemVariable systemVariable = new SystemVariable();
        systemVariable.setJs("Test JS Content");
        assertEquals("Test JS Content", systemVariable.getJs());
    }
}
