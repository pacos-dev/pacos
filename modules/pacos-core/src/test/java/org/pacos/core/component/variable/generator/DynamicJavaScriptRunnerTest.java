package org.pacos.core.component.variable.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicJavaScriptRunnerTest {

    @Test
    void whenCallJsFunctionThenResultIsCorrect() {
        assertEquals("Hello world", DynamicJavaScriptRunner.runCode("return 'Hello world';"));
    }


    @Test
    void whenCallInvalidFunctionThenReturnError() {
        DynamicJavaScriptException ex = assertThrows(DynamicJavaScriptException.class,
                () -> DynamicJavaScriptRunner.runCode("return 'test'+=="));
        assertTrue(ex.getMessage().contains("Expected an operand but found ="));
    }
}