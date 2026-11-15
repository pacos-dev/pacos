package org.pacos.core.component.user.view.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.window.DesktopWindow;
import org.pacos.core.component.user.view.UserChangePasswordPanel;

class UserPasswordConfigTest {

    private final UserPasswordConfig userPasswordConfig = new UserPasswordConfig();

    @Test
    void whenGetTitleThenReturnCorrectTitle() {
        String title = userPasswordConfig.title();
        assertEquals("User config", title);
    }

    @Test
    void whenGetIconThenReturnCorrectIconUrl() {
        String iconUrl = userPasswordConfig.icon();
        assertEquals(PacosIcon.USER_SETTING.getUrl(), iconUrl);
    }

    @Test
    void whenGetActivatorClassThenReturnUserConfigPanelClass() {
        Class<? extends DesktopWindow> activatorClass = userPasswordConfig.activatorClass();
        assertEquals(UserChangePasswordPanel.class, activatorClass);
    }

    @Test
    void whenIsApplicationThenReturnFalse() {
        boolean isApplication = userPasswordConfig.isApplication();
        assertFalse(isApplication);
    }

    @Test
    void whenIsAllowMultipleInstanceThenReturnFalse() {
        boolean allowMultipleInstance = userPasswordConfig.isAllowMultipleInstance();
        assertFalse(allowMultipleInstance);
    }
}
