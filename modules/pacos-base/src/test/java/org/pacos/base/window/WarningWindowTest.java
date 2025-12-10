package org.pacos.base.window;

import org.junit.jupiter.api.Test;
import org.pacos.base.mock.VaadinMock;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.config.impl.WarningWindowConfig;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WarningWindowTest {

    @Test
    void whenCreateWarningWindowThenNoException() {
        VaadinMock.mockSystem();
        assertNotNull(UserSession.getCurrent());

        WarningWindowConfig windowConfig = new WarningWindowConfig("Warning", "Are you sure?");
        //when
        assertDoesNotThrow(() -> new WarningWindow(windowConfig));
    }
}