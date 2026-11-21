package org.pacos.core.system.window;

import org.config.VaadinMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.settings.view.config.OnboardingWindowConfig;
import org.pacos.core.component.user.proxy.UserProxyService;

import com.vaadin.flow.internal.CurrentInstance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OnboardingWindowTest {

    @AfterEach
    void clearVaadinContext() {
        CurrentInstance.clearAll();
    }

    @Test
    void whenWindowIsCreatedThenRenderGettingStartedGuide() {
        VaadinMock.mockSystem();

        OnboardingWindow window = new OnboardingWindow(
                new OnboardingWindowConfig(), mock(RegistryProxy.class), mock(UserProxyService.class), new PluginState());

        assertNotNull(window);
    }

    @Test
    void whenSessionIsNullThenIsEligibleUserReturnsFalse() {
        assertFalse(OnboardingWindow.isEligibleUser(null));
    }

    @Test
    void whenSessionHasNoUserThenIsEligibleUserReturnsFalse() {
        UserSession session = mock(UserSession.class);

        assertFalse(OnboardingWindow.isEligibleUser(session));
    }

    @Test
    void whenSessionIsGuestThenIsEligibleUserReturnsFalse() {
        UserSession session = mock(UserSession.class);
        UserDTO user = mock(UserDTO.class);
        when(session.getUser()).thenReturn(user);
        when(user.isGuestSession()).thenReturn(true);

        assertFalse(OnboardingWindow.isEligibleUser(session));
    }

    @Test
    void whenSessionBelongsToAdminThenIsEligibleUserReturnsTrue() {
        UserSession session = mock(UserSession.class);
        UserDTO user = mock(UserDTO.class);
        when(session.getUser()).thenReturn(user);
        when(user.isAdminSession()).thenReturn(true);

        assertTrue(OnboardingWindow.isEligibleUser(session));
    }

    @Test
    void whenSessionBelongsToRootUserThenIsEligibleUserReturnsTrue() {
        UserSession session = mock(UserSession.class);
        UserDTO user = mock(UserDTO.class);
        when(session.getUser()).thenReturn(user);
        when(session.isRootUser()).thenReturn(true);

        assertTrue(OnboardingWindow.isEligibleUser(session));
    }

    @Test
    void whenSessionBelongsToRegularUserThenIsEligibleUserReturnsFalse() {
        UserSession session = mock(UserSession.class);
        UserDTO user = mock(UserDTO.class);
        when(session.getUser()).thenReturn(user);
        when(user.isGuestSession()).thenReturn(false);
        when(user.isAdminSession()).thenReturn(false);
        when(session.isRootUser()).thenReturn(false);

        assertFalse(OnboardingWindow.isEligibleUser(session));
    }
}
