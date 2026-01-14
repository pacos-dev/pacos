package org.pacos.core.component.dock.view.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.config.util.TestWindowConfig;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.core.component.plugin.manager.PluginResource;
import org.pacos.core.component.registry.proxy.RegistryProxy;

class DockSettingsTest {

    @Test
    void whenInitializeThenDisplayAllApplicationIcon() {
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        when(registryProxy.isSingleMode()).thenReturn(false);
        try (MockedStatic<PluginResource> pluginMock = mockStatic(PluginResource.class)) {
            TestWindowConfig config = new TestWindowConfig();
            TestWindowConfig config2 = new TestWindowConfig();
            pluginMock.when(PluginResource::getAllWindowConfig).thenReturn(Set.of(config, config2));
            //when
            DockSettings settingPage = new DockSettings(registryProxy);
            //then
            assertEquals(2, settingPage.appIconContent.getChildren().count());

        }

    }
}