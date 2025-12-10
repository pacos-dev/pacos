package org.pacos.core.component.user.view;

import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.user.view.config.UserGuestSessionConfig;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class UserGuestSessionPanelTest {

    @Test
    void whenInitializeThenNoException() {
        VaadinMock.mockSystem();

        assertDoesNotThrow(() -> new UserGuestSessionPanel(new UserGuestSessionConfig(), ProxyMock.userProxyService()));
    }
}