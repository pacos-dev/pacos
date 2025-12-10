package org.pacos.core.component.user.view;

import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.user.view.config.UserConfig;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class UserConfigPanelTest {

    @Test
    void whenInitializeThenNoException() {
        VaadinMock.mockSystem();

        assertDoesNotThrow(() -> new UserConfigPanel(new UserConfig(), ProxyMock.userProxyService()));
    }

}