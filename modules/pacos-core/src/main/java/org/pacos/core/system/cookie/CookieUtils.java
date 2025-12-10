package org.pacos.core.system.cookie;

import java.util.Optional;

import jakarta.servlet.http.Cookie;

import com.vaadin.flow.server.VaadinService;

public class CookieUtils {

    public static final String TOKEN = "token";

    private CookieUtils() {

    }

    public static boolean isRequest() {
        return VaadinService.getCurrentRequest() != null;
    }

    public static boolean isResponse() {
        return VaadinService.getCurrentResponse() != null;
    }

    public static Optional<Cookie> getCookie(String name) {
        if (!isRequest()) {
            return Optional.empty();
        }
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }

    public static boolean setCookie(String name, String value, int maxAge) {
        if (!isResponse()) {
            return false;
        }

        Cookie cookie = new Cookie(name, value);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        VaadinService.getCurrentResponse().addCookie(cookie);

        return true;
    }

    public static boolean removeCookie(String name) {
        if (!isResponse()) {
            return false;
        }

        Cookie cookie = new Cookie(name, "");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        VaadinService.getCurrentResponse().addCookie(cookie);

        return true;
    }
}
