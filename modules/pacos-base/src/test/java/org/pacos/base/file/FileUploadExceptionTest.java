package org.pacos.base.file;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FileUploadExceptionTest {

    @Test
    void whenMessageAndCauseProvidedThenExceptionContainsMessageAndCause() {
        String message = "Failed to save file";
        Exception cause = new Exception("IO error");

        FileUploadException exception = new FileUploadException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void whenOnlyCauseProvidedThenExceptionContainsCauseAndNullMessage() {
        Exception cause = new Exception("Null pointer exception");

        FileUploadException exception = new FileUploadException(null, cause);

        assertNull(exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
