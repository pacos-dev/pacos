package org.pacos.core.component.plugin.view.window;

import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.plugin.proxy.PluginProxy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AppStoreLayoutTest {

    @Test
    void whenInitializedThenNoException() {
        PluginProxy pluginProxy = ProxyMock.pluginProxyMock();
        VaadinMock.mockSystem();
        //when
        assertDoesNotThrow(() -> new AppStoreLayout(pluginProxy));
    }
}