package org.pacos.core.system.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.manager.ShortcutManager;
import org.pacos.config.property.ApplicationProperties;
import org.pacos.config.property.PropertyName;
import org.pacos.core.component.plugin.manager.PluginResource;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.service.provider.VariableProviderHandler;
import org.pacos.core.component.variable.view.config.VariableManagerImpl;
import org.pacos.core.config.session.UserSessionService;
import org.pacos.core.system.proxy.AppProxy;
import org.springframework.context.ApplicationContext;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.VaadinSession;

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

    @AfterEach
    void clearCurrentUi() {
        UI.setCurrent(null);
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
            verify(UI.getCurrent().getPage()).addStyleSheet("/css/index.css");
            verify(UI.getCurrent().getPage()).addJavaScript("/js/system.js");
            verify(UI.getCurrent().getPage()).addJavaScript("/js/clock/clock.js");
            verify(UI.getCurrent().getPage()).addJavaScript("/js/drag/dragscript.js");
        }
    }

    @Test
    void whenDesktopAttachedWithAnalyticsIdThenAddEncodedAnalyticsScript() {
        Properties properties = new Properties();
        properties.setProperty(PropertyName.GA4_MEASUREMENT_ID.getPropertyName(), " G-ABC/123 ");
        Page page = UI.getCurrent().getPage();

        try (MockedStatic<UserSessionService> userSession = mockStatic(UserSessionService.class);
             MockedStatic<ApplicationProperties> applicationProperties = mockStatic(ApplicationProperties.class)) {
            userSession.when(() -> UserSessionService.isLogIn(appProxy.getUserProxyService())).thenReturn(true);
            applicationProperties.when(ApplicationProperties::get).thenReturn(properties);

            DesktopView view = new DesktopView(appProxy);
            view.onAttachedEvent(new VariableManagerImpl(Set.of()));

            verify(page).addJavaScript("/js/analytics/ga4.js?measurementId=G-ABC%2F123");
        }
    }

    @Test
    void whenDesktopAttachedWithoutAnalyticsIdThenDoNotAddAnalyticsScript() {
        Properties properties = new Properties();
        Page page = UI.getCurrent().getPage();

        try (MockedStatic<UserSessionService> userSession = mockStatic(UserSessionService.class);
             MockedStatic<ApplicationProperties> applicationProperties = mockStatic(ApplicationProperties.class)) {
            userSession.when(() -> UserSessionService.isLogIn(appProxy.getUserProxyService())).thenReturn(true);
            applicationProperties.when(ApplicationProperties::get).thenReturn(properties);

            DesktopView view = new DesktopView(appProxy);
            view.onAttachedEvent(new VariableManagerImpl(Set.of()));

            verify(page, never()).addJavaScript(contains("/js/analytics/ga4.js"));
        }
    }

    @Test
    void whenDesktopDetachedThenRestoreUiSystemAndRefreshShortcuts() {
        DesktopView view = new DesktopView(appProxy);
        UserSession userSession = UserSession.getCurrent();
        UI detachedUI = mock(UI.class);
        UI attachedUI = mock(UI.class);
        UISystem uiSystem = mock(UISystem.class);
        ShortcutManager shortcutManager = mock(ShortcutManager.class);
        when(uiSystem.getShortcutManager()).thenReturn(shortcutManager);

        UI.setCurrent(detachedUI);
        userSession.setUiSystem(uiSystem);
        UI.setCurrent(attachedUI);
        DetachEvent event = mock(DetachEvent.class);
        when(event.getUI()).thenReturn(detachedUI);

        view.onDetachEvent(event);

        assertSame(uiSystem, userSession.getUISystem(attachedUI));
        verify(shortcutManager).refreshShortcuts();
    }
}
