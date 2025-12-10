package org.pacos.base.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PacosExceptionTest {

    @Test
    void testExceptionMessage() {
        String message = "Test message";
        PacosException exception = new PacosException(message);

        assertEquals(message, exception.getMessage(), "Exception message should match the provided message");
    }

    @Test
    void testExceptionCause() {
        Throwable cause = new NullPointerException("Cause of exception");
        PacosException exception = new PacosException(cause);

        assertEquals(cause, exception.getCause(), "Exception cause should match the provided cause");
    }

    @Test
    void testExceptionMessageFromCause() {
        Throwable cause = new NullPointerException("Cause of exception");
        PacosException exception = new PacosException(cause);

        assertEquals("java.lang.NullPointerException: Cause of exception", exception.getMessage(),
                "Exception message should match the message from the cause");
    }
}