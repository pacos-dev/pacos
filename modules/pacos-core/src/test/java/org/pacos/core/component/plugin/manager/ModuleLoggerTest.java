package org.pacos.core.component.plugin.manager;

import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockitoAnnotations;
import org.pacos.config.property.PropertyName;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModuleLoggerTest {

    private ModuleLogger moduleLogger;
    private Path logFilePath;

    @TempDir
    private Path rootDir;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        System.setProperty(PropertyName.WORKING_DIR.getPropertyName(), rootDir.toString());
        String pluginName = "testPlugin";
        moduleLogger = new ModuleLogger(pluginName);
        logFilePath = rootDir.resolve(Path.of("module", pluginName, "log"));
        logFilePath.toFile().mkdirs();
        logFilePath = logFilePath.resolve("plugin_initialization.log");
    }

    @AfterEach
    public void tearDown() {
        moduleLogger.stopLogger();
        try {
            Files.deleteIfExists(logFilePath);
        } catch (IOException e) {
            // Ignore
        }
    }

    @Test
    public void whenModuleLoggerInitializedThenLogFileCreated() {
        assertTrue(Files.exists(logFilePath));
    }

    @Test
    public void whenLogMessageThenFileContainsLog() throws IOException {
        Logger logger = moduleLogger.getLogger();
        logger.info("Test log entry");

        String logContent = Files.readString(logFilePath);
        assertTrue(logContent.contains("Test log entry"));
    }

    @Test
    public void whenStopLoggerThenNoMoreLogging() throws IOException {
        moduleLogger.stopLogger();
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.info("Should not be logged");

        String logContent = Files.readString(logFilePath);
        assertFalse(logContent.contains("Should not be logged"));
    }

    @Test
    public void whenLogBelowInfoThenNotCaptured() throws IOException {
        Logger logger = moduleLogger.getLogger();
        logger.debug("Debug message");
        logger.trace("Trace message");

        String logContent = Files.readString(logFilePath);
        assertFalse(logContent.contains("Debug message"));
        assertFalse(logContent.contains("Trace message"));
    }

    @Test
    public void whenLogAtInfoOrAboveThenCaptured() throws IOException {
        Logger logger = moduleLogger.getLogger();
        logger.info("Info message");
        logger.warn("Warning message");
        logger.error("Error message");

        String logContent = Files.readString(logFilePath);
        assertTrue(logContent.contains("Info message"));
        assertTrue(logContent.contains("Warning message"));
        assertTrue(logContent.contains("Error message"));
    }
}
