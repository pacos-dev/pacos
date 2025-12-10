package org.pacos.base.window.config.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.window.WarningWindow;
import org.pacos.base.window.WindowState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarningWindowConfigTest {
    private WarningWindowConfig config;

    @BeforeEach
    void setUp() {
        config = new WarningWindowConfig("Test Title", "Test Message");
    }

    @Test
    void whenTitleCalledThenReturnWarning() {
        assertEquals("Warning", config.title());
    }

    @Test
    void whenIconCalledThenReturnAlertIconUrl() {
        assertEquals(PacosIcon.ALERT.getUrl(), config.icon());
    }

    @Test
    void whenActivatorClassCalledThenReturnWarningWindowClass() {
        assertEquals(WarningWindow.class, config.activatorClass());
    }

    @Test
    void whenIsApplicationCalledThenReturnFalse() {
        assertFalse(config.isApplication());
    }

    @Test
    void whenIsAllowMultipleInstanceCalledThenReturnTrue() {
        assertTrue(config.isAllowMultipleInstance());
    }

    @Test
    void whenGetMessageCalledThenReturnExpectedMessage() {
        assertEquals("Test Message", config.getMessage());
    }

    @Test
    void whenGetTitleCalledThenReturnExpectedTitle() {
        assertEquals("Test Title", config.getTitle());
    }

    @Test
    void whenGetWindowStateCalledThenReturnExpectedState() {
        WindowState state = config.getWindowState();
        assertNotNull(state);
    }
}