package org.pacos.core.system.window;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.core.system.window.config.RestartWindowConfig;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RestartWindowTest {

    @Test
    void whenInitializeThenNoException() {
        VaadinMock.mockSystem();
        assertDoesNotThrow(() -> new RestartWindow(new RestartWindowConfig()));
    }

}