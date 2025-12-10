package org.pacos.base.window.config.impl;

import org.junit.jupiter.api.Test;
import org.pacos.base.component.icon.PacosIcon;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfirmationWindowConfigTest {

    @Test
    void whenInitializeThenNoException() {
        assertDoesNotThrow(() -> new ConfirmationWindowConfig(() -> false));
    }

    @Test
    void whenInitializeThenTitleIsSet() {
        ConfirmationWindowConfig window = new ConfirmationWindowConfig(() -> false);
        assertEquals("Confirmation", window.title());
    }

    @Test
    void whenInitializeThenIconIsSet() {
        ConfirmationWindowConfig window = new ConfirmationWindowConfig(() -> false);
        assertEquals(PacosIcon.QUESTION.getUrl(), window.icon());
    }
}