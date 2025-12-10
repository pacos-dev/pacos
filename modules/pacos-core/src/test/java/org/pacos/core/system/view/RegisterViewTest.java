package org.pacos.core.system.view;

import java.util.stream.Stream;

import com.vaadin.flow.router.BeforeEnterEvent;
import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.config.session.UserSessionService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class RegisterViewTest {

    @Test
    void whenInitializeThenNoException() {
        VaadinMock.mockSystem();
        RegistryProxy registryMock = ProxyMock.registryProxy();
        doReturn(true).when(registryMock).isRegistrationPanelEnabled();

        assertDoesNotThrow(() -> new RegisterView(registryMock, ProxyMock.userProxyService()));
    }

    private static Stream<Arguments> redirectConditions() {
        return Stream.of(
                Arguments.of(true, true, true),
                Arguments.of(false, true, true),
                Arguments.of(false, false, true),
                Arguments.of(true, false, false)
        );
    }

    @ParameterizedTest
    @MethodSource("redirectConditions")
    void whenRegistrationEnabledAndLoginThenRedirect(boolean registrationEnabled, boolean isLogin, boolean redirect) {
        VaadinMock.mockSystem();
        RegistryProxy registryMock = ProxyMock.registryProxy();
        doReturn(true).when(registryMock).isInstalled();
        doReturn(registrationEnabled).when(registryMock).isRegistrationPanelEnabled();
        RegisterView registerView = new RegisterView(registryMock, ProxyMock.userProxyService());
        BeforeEnterEvent enterEvent = mock(BeforeEnterEvent.class);
        try (MockedStatic<UserSessionService> mock = mockStatic(UserSessionService.class)) {
            mock.when(() -> UserSessionService.isLogIn(any())).thenReturn(isLogin);
            //when
            registerView.beforeEnter(enterEvent);
            //then
            if (redirect) {
                verify(enterEvent).forwardTo(IndexView.class);
            } else {
                verifyNoInteractions(enterEvent);
            }
        }
    }

    @Test
    void whenIsNotInstalledThenRedirect() {
        VaadinMock.mockSystem();
        RegistryProxy registryMock = ProxyMock.registryProxy();
        doReturn(false).when(registryMock).isInstalled();
        doReturn(true).when(registryMock).isRegistrationPanelEnabled();
        RegisterView registerView = new RegisterView(registryMock, ProxyMock.userProxyService());
        BeforeEnterEvent enterEvent = mock(BeforeEnterEvent.class);
        try (MockedStatic<UserSessionService> mock = mockStatic(UserSessionService.class)) {
            mock.when(() -> UserSessionService.isLogIn(any())).thenReturn(true);
            //when
            registerView.beforeEnter(enterEvent);

            verify(enterEvent).forwardTo(IndexView.class);
        }
    }

}