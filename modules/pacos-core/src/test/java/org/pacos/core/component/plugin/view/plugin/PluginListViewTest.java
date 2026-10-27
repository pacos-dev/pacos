package org.pacos.core.component.plugin.view.plugin;

import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.proxy.PluginProxy;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginListViewTest {

    private PluginProxy pluginProxy;
    private PluginListView listView;

    @BeforeEach
    void setUp() {
        VaadinMock.mockSystem();
        pluginProxy = ProxyMock.pluginProxyMock();
        listView = new PluginListView(pluginProxy, true);
    }

    @Test
    void whenPluginListIsNullThenNoContentIsShown() {
        listView.listLoaded(null);

        assertTrue(listView.getChildren().anyMatch(org.pacos.base.component.NoContent.class::isInstance));
    }

    @Test
    void whenPluginListIsEmptyThenNoContentIsShown() {
        listView.listLoaded(List.of());

        assertTrue(listView.getChildren().anyMatch(org.pacos.base.component.NoContent.class::isInstance));
    }

    @Test
    void whenPluginListContainsPluginThenSplitViewIsShown() {
        listView.listLoaded(List.of(new Plugin("org.pacos", "test", "", "1.0", "", "")));

        assertTrue(listView.getChildren().anyMatch(com.vaadin.flow.component.splitlayout.SplitLayout.class::isInstance));
    }

    @Test
    void whenPluginListIsLoadedThenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> listView.listLoaded(null));
    }
}
