package org.pacos.core.component.plugin.view.window.tab;

import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.plugin.view.tab.InstallPluginContentLayout;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class UploadPluginViewTest {
    @Test
    void whenInitializedThenNoException() {
        PluginProxy pluginProxy = ProxyMock.pluginProxyMock();
        PluginManager pluginManager = Mockito.mock(PluginManager.class);
        VaadinMock.mockSystem();
        //when
        assertDoesNotThrow(() -> new InstallPluginContentLayout(pluginManager, pluginProxy));
    }
}