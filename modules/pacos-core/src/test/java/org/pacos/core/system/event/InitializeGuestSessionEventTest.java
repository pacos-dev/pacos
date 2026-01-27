package org.pacos.core.system.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.pacos.core.config.session.UserSessionService;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

class InitializeGuestSessionEventTest {

    @Test
    void whenTriggerEventThenRedirectToDesktopPage() {
        UI ui = mock(UI.class);
        VaadinSession session = mock(VaadinSession.class);
        VaadinSession.setCurrent(session);

        InitializeGuestSessionEvent.fireEvent(mock(UserSessionService.class), ui);

        verify(ui).navigate("desktop");
    }
}