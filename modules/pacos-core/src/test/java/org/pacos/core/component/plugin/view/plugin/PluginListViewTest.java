package org.pacos.core.component.plugin.view.plugin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import java.util.List;

import com.vaadin.flow.component.UI;
import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.proxy.PluginProxy;

class PluginListViewTest {

    @AfterEach
    void clearCurrentUi() {
        UI.setCurrent(null);
    }

    @Test
    void whenPluginListIsNullThenShowEmptyStateWithoutThrowing() {
        VaadinMock.mockSystem();
        PluginProxy pluginProxy = ProxyMock.pluginProxyMock();
        when(pluginProxy.getPluginService().findAll()).thenReturn(List.of());

        try (MockedStatic<NotificationUtils> notifications = Mockito.mockStatic(NotificationUtils.class)) {
            PluginListView view = new PluginListView(pluginProxy);

            assertDoesNotThrow(() -> {
                view.listLoaded(null);
                VaadinMock.executeAllUiCommands();
            });
        }
    }

    @Test
    void whenPluginListContainsPluginsThenRenderMarketplaceWithoutThrowing() {
        VaadinMock.mockSystem();
        PluginProxy pluginProxy = ProxyMock.pluginProxyMock();
        when(pluginProxy.getPluginService().findAll()).thenReturn(List.of());
        Plugin plugin = new Plugin("org.pacos", "test-plugin", "", "1.0", "", "");

        PluginListView view = new PluginListView(pluginProxy);

        assertDoesNotThrow(() -> {
            view.listLoaded(List.of(plugin));
            VaadinMock.executeAllUiCommands();
        });
    }
}
