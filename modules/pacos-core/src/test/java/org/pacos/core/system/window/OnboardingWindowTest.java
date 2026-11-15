package org.pacos.core.system.window;

import org.junit.jupiter.api.Test;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OnboardingWindowTest {

    @Test
    void whenSessionIsNullThenIsSetupOwnerReturnsFalse() {
        assertFalse(OnboardingWindow.isSetupOwner(null, mock(RegistryProxy.class)));
    }

    @Test
    void whenSessionIsGuestThenIsSetupOwnerReturnsFalse() {
        UserSession session = mock(UserSession.class);
        UserDTO user = mock(UserDTO.class);
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        when(session.getUser()).thenReturn(user);
        when(user.isGuestSession()).thenReturn(true);

        assertFalse(OnboardingWindow.isSetupOwner(session, registryProxy));

        verify(registryProxy, never()).readRegistry(RegistryName.SETUP_OWNER_USER_ID, Integer.class, null);
    }

    @Test
    void whenCurrentUserIsNotSetupOwnerThenIsSetupOwnerReturnsFalse() {
        UserSession session = mock(UserSession.class);
        UserDTO user = mock(UserDTO.class);
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        when(session.getUser()).thenReturn(user);
        when(user.isGuestSession()).thenReturn(false);
        when(session.getUserId()).thenReturn(12);
        when(registryProxy.readRegistry(RegistryName.SETUP_OWNER_USER_ID, Integer.class, null)).thenReturn(7);

        assertFalse(OnboardingWindow.isSetupOwner(session, registryProxy));
    }

    @Test
    void whenSetupOwnerHasNotDismissedGuideThenIsSetupOwnerReturnsTrue() {
        UserSession session = mock(UserSession.class);
        UserDTO user = mock(UserDTO.class);
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        when(session.getUser()).thenReturn(user);
        when(user.isGuestSession()).thenReturn(false);
        when(session.getUserId()).thenReturn(7);
        when(registryProxy.readRegistry(RegistryName.SETUP_OWNER_USER_ID, Integer.class, null)).thenReturn(7);
        when(registryProxy.readBoolean(RegistryName.ONBOARDING_DISMISSED, false)).thenReturn(false);

        assertTrue(OnboardingWindow.isSetupOwner(session, registryProxy));
    }

    @Test
    void whenSetupOwnerHasDismissedGuideThenIsSetupOwnerReturnsFalse() {
        UserSession session = mock(UserSession.class);
        UserDTO user = mock(UserDTO.class);
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        when(session.getUser()).thenReturn(user);
        when(user.isGuestSession()).thenReturn(false);
        when(session.getUserId()).thenReturn(7);
        when(registryProxy.readRegistry(RegistryName.SETUP_OWNER_USER_ID, Integer.class, null)).thenReturn(7);
        when(registryProxy.readBoolean(RegistryName.ONBOARDING_DISMISSED, false)).thenReturn(true);

        assertFalse(OnboardingWindow.isSetupOwner(session, registryProxy));
    }
}
