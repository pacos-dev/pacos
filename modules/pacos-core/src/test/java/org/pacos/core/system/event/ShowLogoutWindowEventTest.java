package org.pacos.core.system.event;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.base.event.UISystem;
import org.pacos.core.system.window.config.LogoutWindowConfig;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class ShowLogoutWindowEventTest {

    @Test
    void whenFireEventThenDisplayLogoutWindow() {
        VaadinMock.mockSystem();
        //when
        ShowLogoutWindowEvent.fireEvent(UISystem.getCurrent());
        //then
        verify(UISystem.getCurrent().getWindowManager()).showWindow(LogoutWindowConfig.class);
        verify(UISystem.getCurrent().getWindowManager()).showAndMoveToFront(any());

    }
}