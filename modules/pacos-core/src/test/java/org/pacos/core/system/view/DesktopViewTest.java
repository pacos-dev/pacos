package org.pacos.core.system.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.VaadinSession;
import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.plugin.manager.PluginResource;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.service.provider.VariableProviderHandler;
import org.pacos.core.component.variable.view.config.VariableManagerImpl;
import org.pacos.core.config.session.UserSessionService;
import org.pacos.core.system.proxy.AppProxy;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DesktopViewTest {

    @Mock
    private ApplicationContext context;
    @Mock
    private VariableProviderHandler variableProviderHandler;
    private final AppProxy appProxy = ProxyMock.appProxyMock();


    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        when(context.getBeansOfType(any())).thenReturn(Map.of());
        new PluginResource(context);

        VaadinMock.mockSystem();
//        when(modules.getModules()).thenReturn(List.of(new SettingsConfig()));
        when(variableProviderHandler.getProviders()).thenReturn(List.of());
        when(appProxy.getUserVariableCollectionProxy().createGlobalCollection(2)).thenReturn(UserVariableCollectionDTO.builder()
                .name(UserVariableCollectionDTO.GLOBAL_NAME)
                .global(true)
                .userId(2).variables(new ArrayList<>()).build());
        when(appProxy.getUserVariableCollectionProxy().createGlobalCollection(1)).thenReturn(UserVariableCollectionDTO.builder()
                .name(UserVariableCollectionDTO.GLOBAL_NAME)
                .global(true)
                .userId(1).variables(new ArrayList<>()).build());
    }

    @Test
    void whenInitializeThenNoException() {
        assertDoesNotThrow(() -> new DesktopView(appProxy));
    }

    @Test
    void whenInitializeForGuestThenNoException() {
        VaadinMock.mockSystem(new UserDTO(1, "guest", null));
        assertDoesNotThrow(() -> new DesktopView(appProxy));
    }

    @Test
    void whenIsNotLoginThenRedirectToIndexView() {
        DesktopView view = new DesktopView(appProxy);
        BeforeEnterEvent beforeEnterEvent = mock(BeforeEnterEvent.class);
        try (MockedStatic<UserSessionService> userSession = mockStatic(UserSessionService.class)) {
            userSession.when(() -> UserSessionService.isLogIn(appProxy.getUserProxyService())).thenReturn(false);

            view.beforeEnter(beforeEnterEvent);

            verify(beforeEnterEvent).forwardTo(IndexView.class);
        }
    }

    @Test
    void whenIsLoginThenNoRedirect() {
        DesktopView view = new DesktopView(appProxy);
        BeforeEnterEvent beforeEnterEvent = mock(BeforeEnterEvent.class);
        try (MockedStatic<UserSessionService> userSession = mockStatic(UserSessionService.class)) {
            userSession.when(() -> UserSessionService.isLogIn(appProxy.getUserProxyService())).thenReturn(true);

            view.beforeEnter(beforeEnterEvent);

            verifyNoInteractions(beforeEnterEvent);
        }
    }


    @Test
    void whenIsNotLogInThenUnprivilegedAccess() {
        UI.setCurrent(mock(UI.class));
        try (MockedStatic<UserSessionService> userSession = mockStatic(UserSessionService.class)) {
            userSession.when(() -> UserSessionService.isLogIn(appProxy.getUserProxyService())).thenReturn(false);

            assertTrue(DesktopView.unprivilegedAccess(appProxy.getUserProxyService()));
        }
    }


    @Test
    void whenUserSessionInNullThenUnprivilegedAccess() {
        UI.setCurrent(mock(UI.class));
        CurrentInstance.set(VaadinSession.class, mock(VaadinSession.class));
        try (MockedStatic<UserSessionService> userSession = mockStatic(UserSessionService.class)) {
            userSession.when(() -> UserSessionService.isLogIn(appProxy.getUserProxyService())).thenReturn(true);

            assertTrue(DesktopView.unprivilegedAccess(appProxy.getUserProxyService()));
        }
    }

    @Test
    void whenUserIsNullThenUnprivilegedAccess() {
        UI.setCurrent(mock(UI.class));
        VaadinSession session = mock(VaadinSession.class);
        when(session.getAttribute(UserSession.class)).thenReturn(mock(UserSession.class));
        CurrentInstance.set(VaadinSession.class, session);
        try (MockedStatic<UserSessionService> userSession = mockStatic(UserSessionService.class)) {
            userSession.when(() -> UserSessionService.isLogIn(appProxy.getUserProxyService())).thenReturn(true);

            assertTrue(DesktopView.unprivilegedAccess(appProxy.getUserProxyService()));
        }
    }

    @Test
    void whenDesktopAttachedThenImportEagerResources() {
        try (MockedStatic<UserSessionService> userSession = mockStatic(UserSessionService.class)) {
            userSession.when(() -> UserSessionService.isLogIn(appProxy.getUserProxyService())).thenReturn(true);
            //when
            DesktopView view = new DesktopView(appProxy);
            view.onAttachedEvent(new VariableManagerImpl(Set.of()));
            //then
            verify(UI.getCurrent().getPage()).addStyleSheet("frontend/css/index.css");
            verify(UI.getCurrent().getPage()).addJavaScript("frontend/js/system.js");
            verify(UI.getCurrent().getPage()).addJavaScript("frontend/js/clock/clock.js");
            verify(UI.getCurrent().getPage()).addJavaScript("frontend/js/drag/dragscript.js");
        }
    }

}