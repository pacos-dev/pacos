package org.pacos.core.component.plugin.view.window;

import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.plugin.view.config.AppStoreWindowConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class AppStoreWindowTest {

    @Test
    void whenConstructedThenSetExpectedSizeAndAddStoreLayout() {
        VaadinMock.mockSystem();

        AppStoreWindow window = new AppStoreWindow(new AppStoreWindowConfig(), ProxyMock.pluginProxyMock());

        assertEquals("800.0px", window.getWidth());
        assertEquals("500.0px", window.getHeight());
        assertEquals(1, window.getContent().getComponentCount());
        assertInstanceOf(AppStoreLayout.class, window.getContent().getComponentAt(0));
    }
}