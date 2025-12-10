package org.pacos.core.system.event;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.base.session.UserSession;
import org.pacos.core.system.window.config.RestartWindowConfig;

import static org.mockito.Mockito.verify;

class ShowRestartSystemWindowEventTest {

    @Test
    void whenFireEventIsCalledThenShowAndMoveToFrontIsInvoked() {
        VaadinMock.mockSystem();

        ShowRestartSystemWindowEvent.fireEvent(UserSession.getCurrent().getUISystem());

        verify(UserSession.getCurrent().getUISystem().getWindowManager()).showWindow(RestartWindowConfig.class);
    }
}
