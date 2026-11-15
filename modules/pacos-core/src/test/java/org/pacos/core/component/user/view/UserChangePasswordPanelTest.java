package org.pacos.core.component.user.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.user.view.config.UserPasswordConfig;

class UserChangePasswordPanelTest {

    @Test
    void whenInitializeThenNoException() {
        VaadinMock.mockSystem();

        assertDoesNotThrow(() -> new UserChangePasswordPanel(new UserPasswordConfig(), ProxyMock.userProxyService()));
    }

}