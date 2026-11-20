package org.pacos.base.credential;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CredentialAccessExceptionTest {

    @Test
    void whenCreateWithMessageThenExposeMessage() {
        CredentialAccessException exception = new CredentialAccessException("Access denied");

        assertEquals("Access denied", exception.getMessage());
    }

    @Test
    void whenCreateWithMessageAndCauseThenExposeMessageAndCause() {
        IllegalStateException cause = new IllegalStateException("Underlying failure");
        CredentialAccessException exception = new CredentialAccessException("Access denied", cause);

        assertEquals("Access denied", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
