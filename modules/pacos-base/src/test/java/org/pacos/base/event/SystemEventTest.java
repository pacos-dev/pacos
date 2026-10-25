package org.pacos.base.event;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SystemEventTest {

    @Test
    void whenNotifyCalledThenInvokeSubscribedNotifierWithValue() {
        SystemEvent<TestEvent> systemEvent = new SystemEvent<>();
        AtomicReference<Object> received = new AtomicReference<>();
        Object eventValue = new Object();

        systemEvent.subscribe(TestEvent.UPDATED, received::set);
        systemEvent.notify(TestEvent.UPDATED, eventValue);

        assertSame(eventValue, received.get());
    }

    @Test
    void whenNotifierUnsubscribedThenDoNotReceiveEvents() {
        SystemEvent<TestEvent> systemEvent = new SystemEvent<>();
        AtomicReference<Object> received = new AtomicReference<>();
        Notifier notifier = received::set;
        systemEvent.subscribe(TestEvent.UPDATED, notifier);

        systemEvent.unsubscribe(TestEvent.UPDATED, notifier);
        systemEvent.notify(TestEvent.UPDATED, new Object());

        assertNull(received.get());
    }

    @Test
    void whenNotifyCalledWithoutValueThenInvokeSubscribedNotifierWithNull() {
        SystemEvent<TestEvent> systemEvent = new SystemEvent<>();
        AtomicReference<Object> received = new AtomicReference<>();
        systemEvent.subscribe(TestEvent.UPDATED, received::set);

        systemEvent.notify(TestEvent.UPDATED);

        assertNull(received.get());
    }

    private enum TestEvent implements EventType {
        UPDATED
    }
}
