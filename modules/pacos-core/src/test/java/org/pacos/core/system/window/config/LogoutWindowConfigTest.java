package org.pacos.core.system.window.config;

import org.junit.jupiter.api.Test;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.core.system.window.LogoutWindow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LogoutWindowConfigTest {

    private final LogoutWindowConfig logoutWindowConfig = new LogoutWindowConfig();

    @Test
    void whenTitleCalledThenReturnLogout() {
        assertEquals("Logout", logoutWindowConfig.title());
    }

    @Test
    void whenIconCalledThenReturnLogoutIconUrl() {
        assertEquals(PacosIcon.LOGOUT.getUrl(), logoutWindowConfig.icon());
    }

    @Test
    void whenActivatorClassCalledThenReturnLogoutWindowClass() {
        assertEquals(LogoutWindow.class, logoutWindowConfig.activatorClass());
    }

    @Test
    void whenIsApplicationCalledThenReturnFalse() {
        assertFalse(logoutWindowConfig.isApplication());
    }

    @Test
    void whenIsAllowMultipleInstanceCalledThenReturnFalse() {
        assertFalse(logoutWindowConfig.isAllowMultipleInstance());
    }
}
