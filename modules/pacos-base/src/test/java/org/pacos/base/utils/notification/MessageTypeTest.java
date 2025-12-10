package org.pacos.base.utils.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessageTypeTest {

    @Test
    void whenMessageTypeIsErrorThenIsErrorReturnsTrue() {
        assertTrue(MessageType.ERROR.isError());
    }

    @Test
    void whenMessageTypeIsNotErrorThenIsErrorReturnsFalse() {
        assertFalse(MessageType.INFO.isError());
        assertFalse(MessageType.SUCCESS.isError());
    }

    @Test
    void whenMessageTypeIsSuccessThenIsSuccessReturnsTrue() {
        assertTrue(MessageType.SUCCESS.isSuccess());
    }

    @Test
    void whenMessageTypeIsNotSuccessThenIsSuccessReturnsFalse() {
        assertFalse(MessageType.INFO.isSuccess());
        assertFalse(MessageType.ERROR.isSuccess());
    }
}
