package org.pacos.base.file.archive;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArchiveExceptionTest {

    @Test
    void whenMessageProvidedThenExceptionContainsMessage() {
        String message = "Archive error message";
        ArchiveException exception = new ArchiveException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void whenCauseProvidedThenExceptionContainsCause() {
        Throwable cause = new RuntimeException("Cause of archive error");
        ArchiveException exception = new ArchiveException(cause);
        assertEquals(cause, exception.getCause());
    }
}
