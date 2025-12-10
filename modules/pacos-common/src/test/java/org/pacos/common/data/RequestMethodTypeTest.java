package org.pacos.common.data;

import org.junit.jupiter.api.Test;
import org.pacos.base.component.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequestMethodTypeTest {

    @Test
    void whenGetRequestThenColorIsGreen() {
        assertEquals(Color.GREEN, RequestMethodType.GET.getColor());
    }

    @Test
    void whenPostRequestThenColorIsOrange() {
        assertEquals(Color.ORANGE, RequestMethodType.POST.getColor());
    }

    @Test
    void whenPutRequestThenColorIsLightBlue() {
        assertEquals(Color.LIGHTBLUE, RequestMethodType.PUT.getColor());
    }

    @Test
    void whenDeleteRequestThenColorIsRed() {
        assertEquals(Color.RED, RequestMethodType.DELETE.getColor());
    }

    @Test
    void whenHeadRequestThenColorIsYellow() {
        assertEquals(Color.YELLOW, RequestMethodType.HEAD.getColor());
    }

    @Test
    void whenOptionsRequestThenColorIsBlackLight() {
        assertEquals(Color.BLACK_LITGHT, RequestMethodType.OPTIONS.getColor());
    }

    @Test
    void whenTraceRequestThenColorIsText() {
        assertEquals(Color.TEXT, RequestMethodType.TRACE.getColor());
    }

    @Test
    void whenPathRequestThenColorIsOrange() {
        assertEquals(Color.ORANGE, RequestMethodType.PATCH.getColor());
    }
}
