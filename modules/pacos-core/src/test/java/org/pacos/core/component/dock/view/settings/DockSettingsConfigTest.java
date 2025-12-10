package org.pacos.core.component.dock.view.settings;

import java.util.Map;

import org.config.PluginManagerMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.registry.proxy.RegistryProxy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DockSettingsConfigTest {

    @Test
    void whenGetTitleThenReturnsDock() {
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        DockSettingsConfig config = new DockSettingsConfig(registryProxy);

        assertEquals("Dock", config.getTitle());
    }

    @Test
    void whenGenerateContentThenReturnsDockSettings() {
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        DockSettingsConfig config = new DockSettingsConfig(registryProxy);
        VaadinMock.mockSystem();
        PluginManagerMock.mockPluginResources(Map.of());
        assertNotNull(config.generateContent());

        assertInstanceOf(DockSettings.class, config.generateContent());
    }

    @Test
    void whenGetOrderThenReturnsCorrectValue() {
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        DockSettingsConfig config = new DockSettingsConfig(registryProxy);

        assertEquals(100, config.getOrder());
    }

    @Test
    void whenShouldBeDisplayedThenReturnsFalseForSingleMode() {
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        when(registryProxy.isSingleMode()).thenReturn(true);
        DockSettingsConfig config = new DockSettingsConfig(registryProxy);

        assertFalse(config.shouldBeDisplayed(null));
    }

    @Test
    void whenShouldBeDisplayedThenReturnsTrueForMultiMode() {
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        when(registryProxy.isSingleMode()).thenReturn(false);
        DockSettingsConfig config = new DockSettingsConfig(registryProxy);

        assertTrue(config.shouldBeDisplayed(null));
    }

    @Test
    void whenOnDeleteThenNoException() {
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        when(registryProxy.isSingleMode()).thenReturn(true);
        DockSettingsConfig config = new DockSettingsConfig(registryProxy);
        SettingPageLayout layout = config.generateContent();
        assertDoesNotThrow(()->layout.onShortCutDetected(ShortcutType.DELETE));
    }

    @Test
    void whenOnSaveThenNoException() {
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        when(registryProxy.isSingleMode()).thenReturn(true);
        DockSettingsConfig config = new DockSettingsConfig(registryProxy);

        SettingPageLayout layout = config.generateContent();
        assertDoesNotThrow(()->layout.onShortCutDetected(ShortcutType.SAVE));
    }
}
