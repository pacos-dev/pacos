package org.pacos.core.system.view;

import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.BeforeEnterEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.config.session.UserSessionService;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IndexViewTest {
    @InjectMocks
    private IndexView indexView;
    @Mock
    private UserProxyService userProxyService;
    @Mock
    private RegistryProxy registryProxy;
    @Mock
    private BeforeEnterEvent beforeEnterEvent;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenNotInstalledThenRedirectToInstallPage() {
        when(registryProxy.isInstalled()).thenReturn(false);

        indexView.beforeEnter(beforeEnterEvent);

        verify(beforeEnterEvent).forwardTo(InstallerView.class);
    }

    private static Stream<Arguments> redirectConditions() {
        return Stream.of(
                Arguments.of(true, true, DesktopView.class),
                Arguments.of(true, false, DesktopView.class),
                Arguments.of(false, true, DesktopView.class),
                Arguments.of(false, false, LoginView.class)
        );
    }

    @ParameterizedTest
    @MethodSource("redirectConditions")
    void whenIsLoginAndInstalledThenRedirect(boolean isLogin, boolean autoLogin, Class<? extends Component> redirect) {
        when(registryProxy.isInstalled()).thenReturn(true);

        try (MockedStatic<UserSessionService> userSession = mockStatic(UserSessionService.class)) {
            userSession.when(() -> UserSessionService.autoLogin(userProxyService)).thenReturn(autoLogin);
            userSession.when(() -> UserSessionService.isLogIn(userProxyService)).thenReturn(isLogin);

            indexView.beforeEnter(beforeEnterEvent);

            verify(beforeEnterEvent).forwardTo(redirect);

        }

    }
}