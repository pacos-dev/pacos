package org.pacos.core.system.event;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.user.proxy.UserProxyService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InitializeGuestSessionEventTest {

    @Test
    void whenTriggerEventThenInitializeGuestSession() {
        UI ui = mock(UI.class);
        VaadinSession session = mock(VaadinSession.class);
        VaadinSession.setCurrent(session);

        UserProxyService mock = mock(UserProxyService.class);
        //when
        InitializeGuestSessionEvent.fireEvent(mock, ui);
        //then
        verify(mock).initializeGuestSession();
    }

    @Test
    void whenTriggerEventThenRedirectToDesktopPage() {
        UI ui = mock(UI.class);
        VaadinSession session = mock(VaadinSession.class);
        VaadinSession.setCurrent(session);

        InitializeGuestSessionEvent.fireEvent(mock(UserProxyService.class), ui);

        verify(ui).navigate("desktop");
    }
}