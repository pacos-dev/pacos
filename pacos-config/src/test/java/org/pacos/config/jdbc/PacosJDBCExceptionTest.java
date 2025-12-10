package org.pacos.config.jdbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PacosJDBCExceptionTest {

    @Test
    void testPacosDBCExceptionWithNestedException() {
        Exception nestedException = new Exception("Nested exception message");

        PacosJDBCException exception = new PacosJDBCException(nestedException);

        assertEquals(nestedException, exception.getCause());
        assertEquals("Nested exception message", exception.getCause().getMessage());
    }

    @Test
    void testPacosJDBCExceptionWithoutNestedException() {
        PacosJDBCException exception = assertThrows(PacosJDBCException.class, () -> {
            throw new PacosJDBCException(null);
        });

        assertNull(exception.getCause());
    }
}