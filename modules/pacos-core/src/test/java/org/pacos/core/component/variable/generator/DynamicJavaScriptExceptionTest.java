package org.pacos.core.component.variable.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamicJavaScriptExceptionTest {

    @Test
    void whenCreateExceptionThenMessageIsSet() {
        //when
        String expectedMessage = "Test exception message";

        //when
        DynamicJavaScriptException exception = new DynamicJavaScriptException(expectedMessage);

        //then
        assertEquals(expectedMessage, exception.getMessage());
    }
}