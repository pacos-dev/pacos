package org.pacos.base.camunda;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultVariableTest {

    @Test
    void whenCreatedThenExposeNameAndDescription() {
        ResultVariable resultVariable = new ResultVariable("result", "Calculated result");

        assertEquals("result", resultVariable.name());
        assertEquals("Calculated result", resultVariable.description());
    }
}
