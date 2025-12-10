package org.pacos.starter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LogbackConfigTest {

    @Test
    void testLogbackConfig() throws IOException {
        Path path = File.createTempFile("test", "test").toPath();
        assertDoesNotThrow(() -> LogbackConfig.configure(path));
    }
}