package org.pacos.core.system.window.config;

import org.junit.jupiter.api.Test;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.core.system.window.RestartWindow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RestartWindowConfigTest {

    private final RestartWindowConfig restartWindowConfig = new RestartWindowConfig();

    @Test
    void whenTitleCalledThenReturnRestart() {
        assertEquals("Restart", restartWindowConfig.title());
    }

    @Test
    void whenIconCalledThenReturnLogoutIconUrl() {
        assertEquals(PacosIcon.LOGOUT.getUrl(), restartWindowConfig.icon());
    }

    @Test
    void whenActivatorClassCalledThenReturnRestartWindowClass() {
        assertEquals(RestartWindow.class, restartWindowConfig.activatorClass());
    }

    @Test
    void whenIsApplicationCalledThenReturnFalse() {
        assertFalse(restartWindowConfig.isApplication());
    }

    @Test
    void whenIsAllowMultipleInstanceCalledThenReturnFalse() {
        assertFalse(restartWindowConfig.isAllowMultipleInstance());
    }
}
