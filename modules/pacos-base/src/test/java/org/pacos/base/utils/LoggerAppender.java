
package org.pacos.base.utils;

import java.util.List;
import java.util.stream.Collectors;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

/**
 * This class is a helper appender for logger. It allows you to monitor what has been logged for specific class
 * during test
 */
public class LoggerAppender {

    private final ListAppender<ILoggingEvent> listAppender;

    public LoggerAppender(Class<?> clazz) {
        Logger logger = (Logger) LoggerFactory.getLogger(clazz);
        this.listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    public List<String> getLogs() {
        return listAppender.list.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.toList());
    }

}
