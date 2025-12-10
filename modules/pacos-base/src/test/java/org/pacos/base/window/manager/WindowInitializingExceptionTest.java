package org.pacos.base.window.manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WindowInitializingExceptionTest {

    @Test
    void whenExceptionPassedToConstructorThenCanRetrieveIt() {
        Exception cause = new Exception("Test exception");
        WindowInitializingException exception = new WindowInitializingException(cause);

        assertNotNull(exception.getCause());
        assertEquals(cause, exception.getCause());
        assertEquals("java.lang.Exception: Test exception", exception.getMessage());
    }
}