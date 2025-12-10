package org.pacos.core.component.user.view.config;

import org.junit.jupiter.api.Test;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.window.DesktopWindow;
import org.pacos.core.component.user.view.UserGuestSessionPanel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UserGuestSessionConfigTest {

    private final UserGuestSessionConfig userGuestSessionConfig = new UserGuestSessionConfig();

    @Test
    void whenGetTitleThenReturnCorrectTitle() {
        String title = userGuestSessionConfig.title();
        assertEquals("Guest session", title);
    }

    @Test
    void whenGetIconThenReturnCorrectIconUrl() {
        String iconUrl = userGuestSessionConfig.icon();
        assertEquals(PacosIcon.USER_SETTING.getUrl(), iconUrl);
    }

    @Test
    void whenGetActivatorClassThenReturnUserGuestSessionPanelClass() {
        Class<? extends DesktopWindow> activatorClass = userGuestSessionConfig.activatorClass();
        assertEquals(UserGuestSessionPanel.class, activatorClass);
    }

    @Test
    void whenIsApplicationThenReturnFalse() {
        boolean isApplication = userGuestSessionConfig.isApplication();
        assertFalse(isApplication);
    }

    @Test
    void whenIsAllowMultipleInstanceThenReturnFalse() {
        boolean allowMultipleInstance = userGuestSessionConfig.isAllowMultipleInstance();
        assertFalse(allowMultipleInstance);
    }
}
