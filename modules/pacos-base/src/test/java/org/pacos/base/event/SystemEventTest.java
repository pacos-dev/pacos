package org.pacos.base.event;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.mock.VaadinMock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemEventTest {

    private SystemEvent<EventType> systemEvent;

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();
        this.systemEvent = new SystemEvent<>();
    }

    @Test
    void whenSubscribeThenEventPublisherIsUsed() {
        //given
        AtomicInteger atomicInteger = new AtomicInteger(0);
        systemEvent.subscribe(TestEvent.MY_EVENT, e -> atomicInteger.incrementAndGet());
        //when
        systemEvent.notify(TestEvent.MY_EVENT);
        systemEvent.notify(TestEvent.MY_EVENT, null);
        //then
        assertEquals(2, atomicInteger.get());
    }

    enum TestEvent implements EventType {
        MY_EVENT,
        MY_SECOND_EVENT;
    }
}