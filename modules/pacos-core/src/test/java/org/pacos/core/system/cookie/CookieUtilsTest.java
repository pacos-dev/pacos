package org.pacos.core.system.cookie;

import java.util.Optional;

import jakarta.servlet.http.Cookie;

import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CookieUtilsTest {

    private VaadinRequest vaadinRequestMock;
    private VaadinResponse vaadinResponseMock;

    @BeforeEach
    void setUp() {
        vaadinRequestMock = mock(VaadinRequest.class);
        vaadinResponseMock = mock(VaadinResponse.class);
        VaadinService vaadinServiceMock = mock(VaadinService.class);
        CurrentInstance.set(VaadinService.class, vaadinServiceMock);
        CurrentInstance.set(VaadinRequest.class, vaadinRequestMock);
        CurrentInstance.set(VaadinResponse.class, vaadinResponseMock);
        VaadinService.setCurrent(vaadinServiceMock);
    }

    @Test
    void whenCalledIsRequestWithValidRequestThenReturnTrue() {
        assertTrue(CookieUtils.isRequest());
    }

    @Test
    void whenCalledIsRequestWithNoRequestThenReturnFalse() {
        CurrentInstance.set(VaadinRequest.class, null);
        assertFalse(CookieUtils.isRequest());
    }

    @Test
    void whenCalledIsResponseWithValidResponseThenReturnTrue() {
        assertTrue(CookieUtils.isResponse());
    }

    @Test
    void whenCalledIsResponseWithNoResponseThenReturnFalse() {
        CurrentInstance.set(VaadinResponse.class, null);
        assertFalse(CookieUtils.isResponse());
    }

    @Test
    void whenCalledGetCookieWithExistingCookieThenReturnCookie() {
        Cookie cookie = new Cookie("token", "value");
        when(vaadinRequestMock.getCookies()).thenReturn(new Cookie[]{cookie});

        Optional<Cookie> result = CookieUtils.getCookie("token");

        assertTrue(result.isPresent());
        assertEquals("value", result.get().getValue());
    }

    @Test
    void whenCalledGetCookieWithNonExistingCookieThenReturnEmpty() {
        when(vaadinRequestMock.getCookies()).thenReturn(new Cookie[]{new Cookie("other", "value")});

        Optional<Cookie> result = CookieUtils.getCookie("token");

        assertFalse(result.isPresent());
    }

    @Test
    void whenCalledGetCookieWithNullRequestThenReturnEmpty() {
        CurrentInstance.set(VaadinRequest.class, null);
        Optional<Cookie> result = CookieUtils.getCookie("token");

        assertFalse(result.isPresent());
    }

    @Test
    void whenCalledSetCookieThenCookieIsAdded() {
        boolean result = CookieUtils.setCookie("name", "value", 3600);

        assertTrue(result);
        verify(vaadinResponseMock).addCookie(argThat(cookie ->
                "name".equals(cookie.getName()) &&
                        "value".equals(cookie.getValue()) &&
                        cookie.getMaxAge() == 3600
        ));
    }

    @Test
    void whenCalledRemoveCookieThenCookieIsRemoved() {
        boolean result = CookieUtils.removeCookie("name");

        assertTrue(result);
        verify(vaadinResponseMock).addCookie(argThat(cookie ->
                "name".equals(cookie.getName()) &&
                        "".equals(cookie.getValue()) &&
                        cookie.getMaxAge() == 0
        ));
    }

    @Test
    void whenCalledSetCookieWithoutResponseThenReturnFalse() {
        CurrentInstance.set(VaadinResponse.class, null);
        boolean result = CookieUtils.setCookie("name", "value", 3600);

        assertFalse(result);
    }

    @Test
    void whenCalledRemoveCookieWithoutResponseThenReturnFalse() {
        CurrentInstance.set(VaadinResponse.class, null);
        boolean result = CookieUtils.removeCookie("name");

        assertFalse(result);
    }
}
