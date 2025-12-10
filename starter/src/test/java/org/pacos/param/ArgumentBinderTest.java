package org.pacos.param;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ArgumentBinderTest {

    @AfterEach
    void tearDown() {
        System.clearProperty("test.property");
    }

    @Test
    void whenBindArgsToSystemPropertyThenPropertySet() {
        String[] args = {"test.property=testValue"};
        ArgumentBinder.bindArgsToSystemProperty(args);

        String result = System.getProperty("test.property");

        assertEquals("testValue", result);
    }

    @Test
    void whenBindArgsToSystemPropertyWithInvalidFormatThenNoPropertySet() {
        String[] args = {"invalidFormat"};
        ArgumentBinder.bindArgsToSystemProperty(args);

        String result = System.getProperty("invalidFormat");

        assertNull(result);
    }

    @Test
    void whenBindJavaOptsThenPropertySet() {
        String javaOts = "\"-Dtest.property=testValue\"";
        ArgumentBinder.bindJavaOptToSystemProperty(javaOts);

        String result = System.getProperty("test.property");

        assertEquals("testValue", result);
    }

}
