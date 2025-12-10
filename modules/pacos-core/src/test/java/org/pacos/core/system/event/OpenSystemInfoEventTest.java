package org.pacos.core.system.event;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.base.event.UISystem;
import org.pacos.core.system.window.config.SystemInfoConfig;

import static org.mockito.Mockito.verify;

class OpenSystemInfoEventTest {

    @Test
    void whenFireEventThenDisplaySystemInfoWindow() {
        VaadinMock.mockSystem();
        //when
        OpenSystemInfoEvent.fireEvent(UISystem.getCurrent());
        //then
        verify(UISystem.getCurrent().getWindowManager()).showWindow(SystemInfoConfig.class);
    }
}