package org.pacos.core.component.user.view.config;

import org.junit.jupiter.api.Test;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.window.DesktopWindow;
import org.pacos.core.component.user.view.UserConfigPanel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UserConfigTest {

    private final UserConfig userConfig = new UserConfig();

    @Test
    void whenGetTitleThenReturnCorrectTitle() {
        String title = userConfig.title();
        assertEquals("User config", title);
    }

    @Test
    void whenGetIconThenReturnCorrectIconUrl() {
        String iconUrl = userConfig.icon();
        assertEquals(PacosIcon.USER_SETTING.getUrl(), iconUrl);
    }

    @Test
    void whenGetActivatorClassThenReturnUserConfigPanelClass() {
        Class<? extends DesktopWindow> activatorClass = userConfig.activatorClass();
        assertEquals(UserConfigPanel.class, activatorClass);
    }

    @Test
    void whenIsApplicationThenReturnFalse() {
        boolean isApplication = userConfig.isApplication();
        assertFalse(isApplication);
    }

    @Test
    void whenIsAllowMultipleInstanceThenReturnFalse() {
        boolean allowMultipleInstance = userConfig.isAllowMultipleInstance();
        assertFalse(allowMultipleInstance);
    }
}
