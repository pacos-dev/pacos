package org.pacos.core.component.settings.view.config;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.settings.view.tab.SystemAccessTabLayout;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemSettingsConfigTest {

    private final RegistryProxy registryProxy = Mockito.mock(RegistryProxy.class);
    private final SystemAccessConfig systemSettingsConfig = new SystemAccessConfig(registryProxy);

    @BeforeEach
    void setUp() {
        VaadinMock.mockSystem();
    }
    @Test
    void whenGetTitleThenReturnsSystemAccess() {
        assertEquals("Guest & Registration", systemSettingsConfig.getTitle());
    }

    @Test
    void whenGetContentThenReturnsSystemSettings() {
        VerticalLayout content = systemSettingsConfig.generateContent();
        assertNotNull(content);
        assertInstanceOf(SystemAccessTabLayout.class, content);
    }

    @Test
    void whenGetOrderThenReturns100() {
        assertEquals(100, systemSettingsConfig.getOrder());
    }

    @Test
    void whenOnDeleteThenNoExceptions() {
        SettingPageLayout content = systemSettingsConfig.generateContent();
        assertDoesNotThrow(() -> content.onShortCutDetected(ShortcutType.DELETE));
    }

    @Test
    void whenOnSaveThenNoExceptions() {
        SettingPageLayout content = systemSettingsConfig.generateContent();
        assertDoesNotThrow(() -> content.onShortCutDetected(ShortcutType.SAVE));
    }

    @Test
    void whenShouldBeDisplayedAndNoPermissionThenReturnsFalse() {
        when(registryProxy.isSingleMode()).thenReturn(true);
        UserSession userSession = mock(UserSession.class);
        when(userSession.hasPermission(SystemPermissions.SYSTEM_ACCESS_TAB_VISIBLE)).thenReturn(false);
        //then
        assertFalse(systemSettingsConfig.shouldBeDisplayed(userSession));
    }

    @Test
    void whenShouldBeDisplayedAndPermissionThenReturnsTrue() {
        when(registryProxy.isSingleMode()).thenReturn(false);
        UserSession userSession = mock(UserSession.class);
        when(userSession.hasPermission(SystemPermissions.SYSTEM_ACCESS_TAB_VISIBLE)).thenReturn(true);
        //then
        assertTrue(systemSettingsConfig.shouldBeDisplayed(userSession));
    }
}
