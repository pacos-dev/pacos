package org.pacos.core.system.view;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.config.session.UserSessionService;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;

@ExtendWith(MockitoExtension.class)
class GuestSessionTest {

    @Mock
    private UserProxyService userProxyService;
    @Mock
    private RegistryProxy registryProxy;
    @Mock
    private UserSessionService userSessionService;
    @Mock
    private BeforeEnterEvent beforeEnterEvent;
    @Mock
    private UI ui;

    private GuestSession guestSession;

    @BeforeEach
    void setUp() {
        guestSession = new GuestSession(userProxyService, registryProxy, userSessionService);
    }

    @Test
    void whenSystemNotInstalledThenForwardToInstaller() {
        //given
        when(registryProxy.isInstalled()).thenReturn(false);

        //when
        guestSession.beforeEnter(beforeEnterEvent);

        //then
        verify(beforeEnterEvent).forwardTo(InstallerView.class);
        verifyNoMoreInteractions(beforeEnterEvent);
    }

    @Test
    void whenUserAlreadyLoggedInThenForwardToDesktop() {
        //given
        when(registryProxy.isInstalled()).thenReturn(true);
        try (MockedStatic<UserSessionService> userSessionMock = mockStatic(UserSessionService.class)) {
            userSessionMock.when(() -> UserSessionService.isLogIn(userProxyService)).thenReturn(true);

            //when
            guestSession.beforeEnter(beforeEnterEvent);

            //then
            verify(beforeEnterEvent).forwardTo(DesktopView.class);
        }
    }

    @Test
    void whenGuestModeEnabledThenInitializeGuestSessionAndForwardToDesktop() {
        //given
        when(registryProxy.isInstalled()).thenReturn(true);
        when(registryProxy.isGuestMode()).thenReturn(true);

        try (MockedStatic<UserSessionService> userSessionMock = mockStatic(UserSessionService.class);
             MockedStatic<UI> uiMock = mockStatic(UI.class)) {

            userSessionMock.when(() -> UserSessionService.isLogIn(userProxyService)).thenReturn(false);
            uiMock.when(UI::getCurrent).thenReturn(ui);

            //when
            guestSession.beforeEnter(beforeEnterEvent);

            //then
            verify(beforeEnterEvent).forwardTo(DesktopView.class);
        }
    }

    @Test
    void whenGuestModeDisabledAndNotLoggedInThenForwardToDesktop() {
        //given
        when(registryProxy.isInstalled()).thenReturn(true);
        when(registryProxy.isGuestMode()).thenReturn(false);

        try (MockedStatic<UserSessionService> userSessionMock = mockStatic(UserSessionService.class)) {
            userSessionMock.when(() -> UserSessionService.isLogIn(userProxyService)).thenReturn(false);

            //when
            guestSession.beforeEnter(beforeEnterEvent);

            //then
            verify(beforeEnterEvent).forwardTo(DesktopView.class);
        }
    }
}