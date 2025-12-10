package org.pacos.starter;

import java.nio.file.Path;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.LoggerFactory;

public final class LogbackConfig {

    private LogbackConfig() {
    }

    static void configure(Path load) {
        String logPath = load.resolve("logs").resolve("starter.log").toString();

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        RollingFileAppender<ILoggingEvent> logFileAppender = new RollingFileAppender<>();
        logFileAppender.setContext(loggerContext);
        logFileAppender.setFile(logPath);

        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setParent(logFileAppender);
        rollingPolicy.setFileNamePattern("starter.%i.log.zip");
        rollingPolicy.start();

        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<>();
        triggeringPolicy.setMaxFileSize(FileSize.valueOf("10MB"));

        triggeringPolicy.start();

        PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
        logEncoder.setContext(loggerContext);
        logEncoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        logEncoder.start();

        logFileAppender.setEncoder(logEncoder);

        logFileAppender.setRollingPolicy(rollingPolicy);
        logFileAppender.setTriggeringPolicy(triggeringPolicy);
        logFileAppender.setName("starterLogFileAppender");
        logFileAppender.start();

        Logger rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(logFileAppender);
    }
}
