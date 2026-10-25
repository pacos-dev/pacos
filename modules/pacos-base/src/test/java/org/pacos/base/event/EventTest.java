package org.pacos.base.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class EventTest {

    @Test
    void whenCreatedThenExposeEventTypeAndNotifier() {
        Notifier notifier = value -> {
        };
        Event<TestEventType> event = new Event<>(TestEventType.UPDATED, notifier);

        assertSame(TestEventType.UPDATED, event.eventType());
        assertSame(notifier, event.notifier());
    }

    private enum TestEventType implements EventType {
        UPDATED
    }
}
