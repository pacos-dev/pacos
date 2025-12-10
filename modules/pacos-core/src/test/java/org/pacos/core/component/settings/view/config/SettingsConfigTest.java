package org.pacos.core.component.settings.view.config;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.security.PermissionConfig;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.settings.view.PanelSettings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsConfigTest {

    private final SettingsConfig settingsConfig = new SettingsConfig();

    @Test
    void whenCallTitleThenReturnsSettings() {
        assertEquals("Settings", settingsConfig.title());
    }

    @Test
    void whenCallIconThenReturnsSettingsIconUrl() {
        assertEquals(PacosIcon.SETTINGS.getUrl(), settingsConfig.icon());
    }

    @Test
    void whenCallActivatorClassThenReturnsPanelSettingsClass() {
        assertEquals(PanelSettings.class, settingsConfig.activatorClass());
    }

    @Test
    void whenCallIsApplicationThenReturnsTrue() {
        assertTrue(settingsConfig.isApplication());
    }

    @Test
    void whenCallIsAllowMultipleInstanceThenReturnsFalse() {
        assertFalse(settingsConfig.isAllowMultipleInstance());
    }

    @Test
    void whenCallIsAllowedForCurrentSessionThenReturnsFalseForGuestSession() {
        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            UserDTO mockUser = Mockito.mock(UserDTO.class);
            mockedUserSession.when(UserSession::getCurrent).thenReturn(new UserSession(mockUser));
            Mockito.when(mockUser.isGuestSession()).thenReturn(true);

            assertFalse(settingsConfig.isAllowedForCurrentSession(UserSession.getCurrent()));
        }
    }

    @Test
    void whenCallIsAllowedForCurrentSessionThenReturnsTrueForNonGuestSession() {
        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class)) {
            UserDTO mockUser = Mockito.mock(UserDTO.class);
            mockedUserSession.when(UserSession::getCurrent).thenReturn(new UserSession(mockUser, Set.of(
                    new PermissionConfig(SystemPermissions.SETTINGS_PLUGIN_VISIBLE.getKey(),"ALLOW"))));
            Mockito.when(mockUser.isGuestSession()).thenReturn(false);

            assertTrue(settingsConfig.isAllowedForCurrentSession(UserSession.getCurrent()));
        }
    }
}
