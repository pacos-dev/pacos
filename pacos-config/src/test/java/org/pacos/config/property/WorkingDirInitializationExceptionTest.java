package org.pacos.config.property;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkingDirInitializationExceptionTest {

    @Test
    void testExceptionMessage() {
        String errorMessage = "Failed to initialize working directory";

        WorkingDirInitializationException exception = new WorkingDirInitializationException(errorMessage);

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testExceptionIsThrown() {
        String errorMessage = "Directory not accessible";

        WorkingDirInitializationException exception = assertThrows(
                WorkingDirInitializationException.class,
                () -> {
                    throw new WorkingDirInitializationException(errorMessage);
                }
        );

        assertEquals(errorMessage, exception.getMessage());
    }
}