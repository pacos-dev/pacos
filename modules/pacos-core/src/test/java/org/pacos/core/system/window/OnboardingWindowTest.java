package org.pacos.core.system.window;

import org.config.VaadinMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.base.event.UISystem;
import org.pacos.base.window.manager.WindowManager;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.security.domain.Role;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.settings.view.config.OnboardingWindowConfig;
import org.pacos.core.component.user.proxy.UserProxyService;

import com.vaadin.flow.internal.CurrentInstance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
        when(session.hasRole(Role.ROOT_ROLE)).thenReturn(true);

        assertTrue(OnboardingWindow.isEligibleUser(session));
    }

    @Test
    void whenSessionBelongsToRegularUserThenIsEligibleUserReturnsFalse() {
        UserSession session = mock(UserSession.class);
        UserDTO user = mock(UserDTO.class);
        when(session.getUser()).thenReturn(user);
        when(user.isGuestSession()).thenReturn(false);
        when(user.isAdminSession()).thenReturn(false);
        when(session.hasRole(Role.ROOT_ROLE)).thenReturn(false);

        assertFalse(OnboardingWindow.isEligibleUser(session));
    }

    @Test
    void whenEligibleUserHasNotDismissedGuideThenOpenOnboardingWindow() {
        UserSession session = eligibleSession();
        UserProxyService userProxyService = mock(UserProxyService.class);
        WindowManager windowManager = mock(WindowManager.class);
        UISystem uiSystem = mock(UISystem.class);
        when(userProxyService.isOnboardingDismissed(7)).thenReturn(false);
        when(uiSystem.getWindowManager()).thenReturn(windowManager);

        try (var userSession = mockStatic(UserSession.class);
             var currentUiSystem = mockStatic(UISystem.class)) {
            userSession.when(UserSession::getCurrent).thenReturn(session);
            currentUiSystem.when(UISystem::getCurrent).thenReturn(uiSystem);

            OnboardingWindow.openForEligibleUser(userProxyService);

            verify(userProxyService).isOnboardingDismissed(7);
            verify(windowManager).showWindow(OnboardingWindowConfig.class);
        }
    }

    @Test
    void whenEligibleUserHasDismissedGuideThenDoNotOpenOnboardingWindow() {
        UserSession session = eligibleSession();
        UserProxyService userProxyService = mock(UserProxyService.class);
        when(userProxyService.isOnboardingDismissed(7)).thenReturn(true);

        try (var userSession = mockStatic(UserSession.class);
             var currentUiSystem = mockStatic(UISystem.class)) {
            userSession.when(UserSession::getCurrent).thenReturn(session);

            OnboardingWindow.openForEligibleUser(userProxyService);

            verify(userProxyService).isOnboardingDismissed(7);
            currentUiSystem.verifyNoInteractions();
        }
    }

    @Test
    void whenIneligibleUserHasNotDismissedGuideThenDoNotOpenOnboardingWindow() {
        UserSession session = mock(UserSession.class);
        UserProxyService userProxyService = mock(UserProxyService.class);

        try (var userSession = mockStatic(UserSession.class)) {
            userSession.when(UserSession::getCurrent).thenReturn(session);

            OnboardingWindow.openForEligibleUser(userProxyService);

            verify(userProxyService, never()).isOnboardingDismissed(7);
        }
    }

    @Test
    void whenEligibleUserOpensGuideManuallyThenOpenOnboardingWindow() {
        UserSession session = eligibleSession();
        WindowManager windowManager = mock(WindowManager.class);
        UISystem uiSystem = mock(UISystem.class);
        when(uiSystem.getWindowManager()).thenReturn(windowManager);

        try (var userSession = mockStatic(UserSession.class);
             var currentUiSystem = mockStatic(UISystem.class)) {
            userSession.when(UserSession::getCurrent).thenReturn(session);
            currentUiSystem.when(UISystem::getCurrent).thenReturn(uiSystem);

            OnboardingWindow.openManuallyForEligibleUser();

            verify(windowManager).showWindow(OnboardingWindowConfig.class);
        }
    }

    private UserSession eligibleSession() {
        UserSession session = mock(UserSession.class);
        UserDTO user = mock(UserDTO.class);
        when(session.getUser()).thenReturn(user);
        when(session.getUserId()).thenReturn(7);
        when(user.isGuestSession()).thenReturn(false);
        when(user.isAdminSession()).thenReturn(true);
        return session;
    }

}
