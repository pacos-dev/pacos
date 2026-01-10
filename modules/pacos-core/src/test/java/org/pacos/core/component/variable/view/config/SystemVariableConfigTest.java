package org.pacos.core.component.variable.view.config;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.variable.proxy.SystemVariableProxy;
import org.pacos.core.component.variable.view.global.SystemVariableSettings;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SystemVariableConfigTest {

    private SystemVariableConfig systemVariableConfig;

    @BeforeEach
    void setUp() {
        VaadinMock.mockSystem();
        SystemVariableProxy systemVariableProxyMock = mock(SystemVariableProxy.class);
        systemVariableConfig = new SystemVariableConfig(systemVariableProxyMock);
    }

    @Test
    void whenGetTitleThenReturnsSystemVariables() {
        assertEquals("Variables", systemVariableConfig.getTitle());
    }

    @Test
    void whenGenerateContentThenReturnsSystemVariableSettingsLayout() {
        SettingPageLayout content = systemVariableConfig.generateContent();
        assertNotNull(content);
        assertInstanceOf(SystemVariableSettings.class, content);
    }

    @Test
    void whenGetOrderThenReturnsZero() {
        assertEquals(0, systemVariableConfig.getOrder());
    }

    @Test
    void whenOnDeleteThenNoException() {
        SettingPageLayout layout = systemVariableConfig.generateContent(); // Initialize `systemVariableSettings`.

        assertDoesNotThrow(() -> layout.onShortCutDetected(ShortcutType.DELETE));
    }

    @Test
    void whenOnSaveThenSettingsOnSaveIsCalled() {
        SettingPageLayout layout = systemVariableConfig.generateContent(); // Initialize `systemVariableSettings`.

        assertDoesNotThrow(() -> layout.onShortCutDetected(ShortcutType.SAVE));
    }

    @Test
    void whenShouldBeDisplayedThenReturnsTrue() {
        assertTrue(systemVariableConfig.shouldBeDisplayed(null));
    }
}
