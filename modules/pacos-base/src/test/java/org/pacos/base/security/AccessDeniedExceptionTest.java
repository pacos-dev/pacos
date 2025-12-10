package org.pacos.base.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccessDeniedExceptionTest {

    @Test
    void whenCreateExceptionThenMessageShouldContainAction() {
        String action = "DELETE_USER";
        AccessDeniedException exception = new AccessDeniedException(action);
        assertThat(exception.getMessage()).isEqualTo("Access denied for action: DELETE_USER");
    }

    @Test
    void whenCreateExceptionThenIsInstanceOfRuntimeException() {
        AccessDeniedException exception = new AccessDeniedException("ANY_ACTION");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
