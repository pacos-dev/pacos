package org.pacos.base.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides event broadcasting functionality
 */
public class EventPublisher<T extends EventType> {

    private static final Logger LOG = LoggerFactory.getLogger(EventPublisher.class);

    private final Map<T, List<Notifier>> notifiers = new HashMap<>();

    EventPublisher() {
    }

    void subscribe(T eventType, Notifier notifier) {
        notifiers.computeIfAbsent(eventType, e -> new ArrayList<>()).add(notifier);
    }

    void unsubscribe(T eventType, Notifier notifier) {
        if(notifiers.get(eventType)!=null) {
            notifiers.get(eventType).remove(notifier);
        }
    }

    void notify(T eventType, Object o) {
        if (notifiers.get(eventType) == null) {
            LOG.debug("Listener for event not found {}", eventType);
        } else {
            new ArrayList<>(notifiers.get(eventType)).forEach(e -> {
                try {
                    e.notify(o);
                } catch (Exception ex) {
                    LOG.error("Error during notification for event type: {}",eventType, ex);
                }
            });
        }
    }

    void notify(T eventType) {
        this.notify(eventType, null);
    }

}
