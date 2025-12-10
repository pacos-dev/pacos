package org.pacos.core.component.plugin.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.spi.FilterReply;
import org.pacos.base.exception.PacosException;
import org.pacos.config.property.WorkingDir;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * Creates log during plugin initialization
 */
public class ModuleLogger {

    private final FileAppender<ILoggingEvent> logFileAppender;
    private final Logger rootLogger;
    private final AtomicBoolean capturing = new AtomicBoolean(false);

    ModuleLogger(String pluginName) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        this.logFileAppender = new FileAppender<>();
        logFileAppender.setContext(loggerContext);
        Path logFile = getFilePath(pluginName);
        logFileAppender.setFile(logFile.toString());

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n");
        encoder.start();

        logFileAppender.setEncoder(encoder);
        logFileAppender.setImmediateFlush(true);
        logFileAppender.start();

        this.rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(logFileAppender);

        loggerContext.addTurboFilter(new TurboFilter() {
            @Override
            public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
                if (!capturing.get()) return FilterReply.NEUTRAL;

                if (level.isGreaterOrEqual(Level.INFO)) {
                    return FilterReply.ACCEPT;
                }
                return FilterReply.NEUTRAL;
            }
        });
        capturing.set(true);
    }

    private static Path getFilePath(String pluginName) {
        Path module = WorkingDir.load().resolve("module").resolve(pluginName).resolve("log");
        File logDir = module.toFile();
        if (!logDir.exists() && !logDir.mkdirs()) {
            throw new PacosException("Unable to create directory " + logDir.getAbsolutePath());
        }
        Path logFile = module.resolve("plugin_initialization.log");
        try {
            Files.deleteIfExists(logFile);
        } catch (IOException e) {
            // Ignore
        }
        return logFile;
    }


    void stopLogger() {
        capturing.set(false);
        rootLogger.detachAppender(logFileAppender);
        logFileAppender.stop();
    }

    Logger getLogger() {
        return rootLogger;
    }
}
