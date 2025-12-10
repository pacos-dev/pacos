package org.pacos.core.component.session.service;

import java.util.Set;

import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.vaadin.addons.variablefield.provider.VariableProvider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceListenerTest {

    @BeforeEach
    void init() {
        UISystem uiSystem = VaadinMock.mockSystem();
        assertNotNull(uiSystem);
        VaadinSession session = VaadinSession.getCurrent();
        doReturn(mock(WebBrowser.class)).when(session).getBrowser();
        doNothing().when(session).setAttribute(any(String.class), any(Object.class));
        doNothing().when(session).setAttribute(UserSession.class, null);
    }

    @Test
    void whenInitServiceThenAddListeners() {
        ServiceInitEvent initEvent = mock(ServiceInitEvent.class);
        VaadinService vaadinService = mock(VaadinService.class);
        when(initEvent.getSource()).thenReturn(vaadinService);
        //when
        new ServiceListener().serviceInit(initEvent);
        //then
        verify(vaadinService).addSessionInitListener(any());
        verify(vaadinService).addSessionDestroyListener(any());
    }

    @Test
    void whenInitializeSessionThenAddToStaticMap() {
        VaadinSession session = VaadinSession.getCurrent();
        //when
        ServiceListener.sessionInitListener();
        //then
        assertTrue(ServiceListener.allSessions.contains(session));
    }


    @Test
    void whenDestroySessionThenRemoveFromMap() {
        ServiceListener.sessionInitListener();
        //when
        ServiceListener.sessionDestroyListener();
        //then
        assertFalse(ServiceListener.allSessions.contains(VaadinSession.getCurrent()));
    }

    @Test
    void whenNotifyAllThenAllSessionsReceivedNotification() {
        VaadinSession session = VaadinSession.getCurrent();
        ServiceListener.sessionInitListener();
        //when
        ServiceListener.notifyAll(ModuleEvent.PLUGIN_INSTALLED, null);
        //then
        verify(UISystem.getCurrent()).notify(ModuleEvent.PLUGIN_INSTALLED, null);
        assertTrue(ServiceListener.allSessions.contains(session));
    }

    @Test
    void whenAddRequestHandlerThenExtendVaadinSession() {
        VaadinSession session = VaadinSession.getCurrent();
        ServiceInitEvent initEvent = mock(ServiceInitEvent.class);
        VaadinService vaadinService = mock(VaadinService.class);
        when(initEvent.getSource()).thenReturn(vaadinService);

        new ServiceListener().serviceInit(initEvent);
        ServiceListener.sessionInitListener();

        RequestHandler requestHandler = mock(RequestHandler.class);
        //when
        ServiceListener.addRequestHandler(requestHandler);
        //then
        verify(session).addRequestHandler(requestHandler);
    }

    @Test
    void whenRemoveRequestHandlerThenRemoveFromSession() {
        VaadinSession session = VaadinSession.getCurrent();
        ServiceListener.sessionInitListener();
        RequestHandler requestHandler = mock(RequestHandler.class);
        //when
        ServiceListener.removeRequestHandler(requestHandler);
        //then
        verify(session).removeRequestHandler(requestHandler);
    }


    @Test
    void whenAddVariableProviderThenAddToVariableManager() {
        ServiceInitEvent initEvent = mock(ServiceInitEvent.class);
        VaadinService vaadinService = mock(VaadinService.class);
        when(initEvent.getSource()).thenReturn(vaadinService);

        new ServiceListener().serviceInit(initEvent);
        ServiceListener.sessionInitListener();

        VariableProvider variableProvider = mock(VariableProvider.class);
        Set<VariableProvider> providerSet = Set.of(variableProvider);
        //when
        ServiceListener.addVariableProviders(providerSet);
        //then
        verify(UISystem.getCurrent().getVariableManager()).addProvider(variableProvider);
    }


    @Test
    void whenRemoveVariableProviderThenRemoveFromVariableManager() {
        ServiceInitEvent initEvent = mock(ServiceInitEvent.class);
        VaadinService vaadinService = mock(VaadinService.class);
        when(initEvent.getSource()).thenReturn(vaadinService);

        new ServiceListener().serviceInit(initEvent);
        ServiceListener.sessionInitListener();

        VariableProvider variableProvider = mock(VariableProvider.class);
        Set<VariableProvider> providerSet = Set.of(variableProvider);
        //when
        ServiceListener.removeVariableProviders(providerSet);
        VaadinMock.executeAllUiCommands();
        //then
        verify(UISystem.getCurrent().getVariableManager()).removeProvider(variableProvider);
    }

    @Test
    void whenAddVaadinSessionThenIsExtended() {
        //when
        ServiceListener.addVaadinSession(VaadinSession.getCurrent());
        //then
        assertTrue(ServiceListener.allSessions.contains(VaadinSession.getCurrent()));
    }
}