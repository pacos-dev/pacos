package org.pacos.core.component.plugin.view.config;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.plugin.view.window.AppStoreWindow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppStoreWindowConfigTest {

    private final AppStoreWindowConfig managerAppConfig = new AppStoreWindowConfig();

    @Test
    void whenCallTitleThenReturnsAppStore() {
        assertEquals("App store", managerAppConfig.title());
    }

    @Test
    void whenCallIconThenReturnsAppStoreIconUrl() {
        assertEquals(PacosIcon.APP_STORE.getUrl(), managerAppConfig.icon());
    }

    @Test
    void whenCallActivatorClassThenReturnsAppStoreWindowClass() {
        assertEquals(AppStoreWindow.class, managerAppConfig.activatorClass());
    }

    @Test
    void whenCallIsApplicationThenReturnsTrue() {
        assertTrue(managerAppConfig.isApplication());
    }

    @Test
    void whenCallIsAllowMultipleInstanceThenReturnsFalse() {
        assertFalse(managerAppConfig.isAllowMultipleInstance());
    }

    @Test
    void whenCallIsAllowedForCurrentSessionAndUserIsGuestThenReturnsFalse() {
        VaadinMock.mockSystem(new UserDTO(1, "guest", null));
        assertFalse(managerAppConfig.isAllowedForCurrentSession(UserSession.getCurrent()));
    }

    @Test
    void whenCallIsAllowedForCurrentSessionAndUserIsNotGuestThenReturnsTrue() {
        VaadinMock.mockSystem();

        assertTrue(managerAppConfig.isAllowedForCurrentSession(UserSession.getCurrent()));
    }
}
