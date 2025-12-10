package org.pacos.common.data;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.pacos.base.component.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpStatusCodeTest {

    @Test
    void whenStatusCodeExistsThenOfReturnsCorrectEnum() {
        Optional<HttpStatusCode> status = HttpStatusCode.of(200);
        assertTrue(status.isPresent());
        assertEquals(HttpStatusCode.OK, status.get());
    }

    @Test
    void whenStatusCodeDoesNotExistThenOfReturnsEmpty() {
        Optional<HttpStatusCode> status = HttpStatusCode.of(999);
        assertFalse(status.isPresent());
    }

    @Test
    void whenHttpStatusIsInformationalThenGetColorReturnsOrange() {
        assertEquals(Color.ORANGE, HttpStatusCode.CONTINUE.getColor());
    }

    @Test
    void whenHttpStatusIsSuccessThenGetColorReturnsGreen() {
        assertEquals(Color.GREEN, HttpStatusCode.OK.getColor());
    }

    @Test
    void whenHttpStatusIsRedirectionThenGetColorReturnsYellow() {
        assertEquals(Color.YELLOW, HttpStatusCode.MOVED_PERMANENTLY.getColor());
    }

    @Test
    void whenHttpStatusIsClientErrorThenGetColorReturnsRed() {
        assertEquals(Color.RED, HttpStatusCode.NOT_FOUND.getColor());
    }

    @Test
    void whenHttpStatusIsServerErrorThenGetColorReturnsLightBlue() {
        assertEquals(Color.LIGHTBLUE, HttpStatusCode.INTERNAL_SERVER_ERROR.getColor());
    }

    @Test
    void whenGettingDescriptionThenReturnsCorrectDescription() {
        assertEquals("Not Found", HttpStatusCode.NOT_FOUND.getDescription());
        assertEquals("OK", HttpStatusCode.OK.getDescription());
    }

    @Test
    void whenGettingCodeThenReturnsCorrectCode() {
        assertEquals(404, HttpStatusCode.NOT_FOUND.getCode());
        assertEquals(200, HttpStatusCode.OK.getCode());
    }
}
