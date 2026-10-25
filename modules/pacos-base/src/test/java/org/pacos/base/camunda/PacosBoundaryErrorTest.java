package org.pacos.base.camunda;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PacosBoundaryErrorTest {

    @Test
    void whenCreatedThenExposeErrorCodeAndMessage() {
        PacosBoundaryError error = new PacosBoundaryError("VALIDATION_ERROR", "Invalid value");

        assertEquals("VALIDATION_ERROR", error.getErrorCode());
        assertEquals("Invalid value", error.getErrorMessage());
    }
}
