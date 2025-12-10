package org.pacos.base.event;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EventPublisherTest {

    @Test
    void whenSubscribeEventThenItsTriggeredWhenNotify() {
        EventPublisher<TestEvent> eventPublisher = new EventPublisher<>();
        AtomicInteger atomicInteger = new AtomicInteger();
        atomicInteger.set(0);
        //when
        eventPublisher.subscribe(TestEvent.MY_EVENT, e -> atomicInteger.set((int) e));
        //and when
        eventPublisher.notify(TestEvent.MY_EVENT, 100);
        //then
        assertEquals(100, atomicInteger.get());
    }

    @Test
    void whenNotifyThenSendNullValue() {
        EventPublisher<TestEvent> eventPublisher = new EventPublisher<>();
        AtomicInteger triggerCount = new AtomicInteger();
        triggerCount.set(0);
        //when
        eventPublisher.subscribe(TestEvent.MY_EVENT, e -> {
            triggerCount.set(triggerCount.get() + 1);
            assertNull(e);
        });
        //and when
        eventPublisher.notify(TestEvent.MY_EVENT);
        //then
        assertEquals(1, triggerCount.get());
    }

    @Test
    void whenEventIsNotSubscribedThenNoExceptionWhenNotify() {
        EventPublisher<TestEvent> eventPublisher = new EventPublisher<>();
        assertDoesNotThrow(() -> eventPublisher.notify(TestEvent.MY_EVENT));
    }

    @Test
    void whenEventIsNotSubscribedThenNoExceptionWhenNotifyWithListener() {
        EventPublisher<TestEvent> eventPublisher = new EventPublisher<>();
        assertDoesNotThrow(() -> eventPublisher.notify(TestEvent.MY_EVENT, "100"));
    }

    @Test
    void whenExceptionDuringProcessingEventThenNoImpactOnEventPublisher() {
        EventPublisher<TestEvent> eventPublisher = new EventPublisher<>();
        AtomicInteger triggerCount = new AtomicInteger();
        triggerCount.set(0);
        //when
        eventPublisher.subscribe(TestEvent.MY_EVENT, e -> {
            triggerCount.set(triggerCount.get() + 1);
            throw new RuntimeException("test");
        });
        eventPublisher.subscribe(TestEvent.MY_EVENT, e -> {
            triggerCount.set(triggerCount.get() + 1);
        });
        //when
        assertDoesNotThrow(()->eventPublisher.notify(TestEvent.MY_EVENT));
        //then
        assertEquals(2,triggerCount.get());
    }


    enum TestEvent implements EventType {
        MY_EVENT,
        MY_SECOND_EVENT;
    }
}