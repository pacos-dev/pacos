package org.pacos.core.component.plugin.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.plugin.manager.data.PluginDataLoader;
import org.pacos.core.component.plugin.manager.data.RequestHandlerRegistration;
import org.pacos.core.component.session.service.ServiceListener;

import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.shared.Registration;

class PluginExtensionRegistryTest {

    @Test
    void whenRegisterThenEveryHandlerAndVariableProviderIsRegistered() {
        PluginDataLoader pluginData = mock(PluginDataLoader.class);
        RequestHandler handler = mock(RequestHandler.class);
        Registration registration = mock(Registration.class);
        when(pluginData.getRequestHandlers()).thenReturn(Set.of(handler));
        when(pluginData.getVariableProviders()).thenReturn(Collections.emptySet());

        try (MockedStatic<ServiceListener> serviceListener = mockStatic(ServiceListener.class)) {
            serviceListener.when(() -> ServiceListener.addRequestHandler(handler)).thenReturn(registration);

            new PluginExtensionRegistry().register(pluginData);

            verify(pluginData).addRequestHandlerRegistration(new RequestHandlerRegistration(registration, handler));
            serviceListener.verify(() -> ServiceListener.addVariableProviders(Collections.emptySet()));
        }
    }

    @Test
    void whenNotifyWindowsRemovedThenEveryWindowIsNotified() {
        PluginDataLoader pluginData = mock(PluginDataLoader.class);
        WindowConfig windowConfig = mock(WindowConfig.class);
        when(pluginData.getWindowConfigSet()).thenReturn(Set.of(windowConfig));

        try (MockedStatic<ServiceListener> serviceListener = mockStatic(ServiceListener.class)) {
            new PluginExtensionRegistry().notifyWindowsRemoved(pluginData);
            serviceListener.verify(() -> ServiceListener.notifyAll(ModuleEvent.MODULE_REMOVED, windowConfig));
        }
    }

    @Test
    void whenUnregisterSucceedsThenEveryRegisteredResourceIsRemoved() {
        PluginDataLoader pluginData = mock(PluginDataLoader.class);
        Registration firstRegistration = mock(Registration.class);
        Registration secondRegistration = mock(Registration.class);
        RequestHandler firstHandler = mock(RequestHandler.class);
        RequestHandler secondHandler = mock(RequestHandler.class);
        when(pluginData.getRequestHandlerRegistration()).thenReturn(Set.of(
                new RequestHandlerRegistration(firstRegistration, firstHandler),
                new RequestHandlerRegistration(secondRegistration, secondHandler)));
        when(pluginData.getVariableProviders()).thenReturn(Collections.emptySet());

        try (MockedStatic<ServiceListener> serviceListener = mockStatic(ServiceListener.class)) {
            new PluginExtensionRegistry().unregister(pluginData);
            serviceListener.verify(() -> ServiceListener.removeRequestHandler(firstHandler));
            serviceListener.verify(() -> ServiceListener.removeRequestHandler(secondHandler));
            serviceListener.verify(() -> ServiceListener.removeVariableProviders(Collections.emptySet()));
            verify(firstRegistration).remove();
            verify(secondRegistration).remove();
        }
    }


    @Test
    void whenRegisterHasNoRequestHandlersThenOnlyProvidersAreRegistered() {
        PluginDataLoader pluginData = mock(PluginDataLoader.class);
        when(pluginData.getRequestHandlers()).thenReturn(Collections.emptySet());
        when(pluginData.getVariableProviders()).thenReturn(Collections.emptySet());

        try (MockedStatic<ServiceListener> serviceListener = mockStatic(ServiceListener.class)) {
            new PluginExtensionRegistry().register(pluginData);

            serviceListener.verify(() -> ServiceListener.addVariableProviders(Collections.emptySet()));
            serviceListener.verifyNoMoreInteractions();
        }
    }

    @Test
    void whenUnregisterHasNoResourcesThenVariableProvidersAreStillRemoved() {
        PluginDataLoader pluginData = mock(PluginDataLoader.class);
        when(pluginData.getRequestHandlerRegistration()).thenReturn(Collections.emptySet());
        when(pluginData.getVariableProviders()).thenReturn(Collections.emptySet());

        try (MockedStatic<ServiceListener> serviceListener = mockStatic(ServiceListener.class)) {
            new PluginExtensionRegistry().unregister(pluginData);

            serviceListener.verify(() -> ServiceListener.removeVariableProviders(Collections.emptySet()));
            serviceListener.verifyNoMoreInteractions();
        }
    }

    @Test
    void whenSeveralCleanupOperationsFailThenThrowFirstFailureAndSuppressRemainingFailures() {
        PluginDataLoader pluginData = mock(PluginDataLoader.class);
        Registration firstRegistration = mock(Registration.class);
        Registration secondRegistration = mock(Registration.class);
        RequestHandler firstHandler = mock(RequestHandler.class);
        RequestHandler secondHandler = mock(RequestHandler.class);
        RuntimeException firstFailure = new IllegalStateException("first");
        RuntimeException secondFailure = new IllegalArgumentException("second");
        doThrow(firstFailure).when(firstRegistration).remove();
        doThrow(secondFailure).when(secondRegistration).remove();
        when(pluginData.getRequestHandlerRegistration()).thenReturn(Set.of(
                new RequestHandlerRegistration(firstRegistration, firstHandler),
                new RequestHandlerRegistration(secondRegistration, secondHandler)));
        when(pluginData.getVariableProviders()).thenReturn(Collections.emptySet());

        RuntimeException thrown;
        try (MockedStatic<ServiceListener> serviceListener = mockStatic(ServiceListener.class)) {
            thrown = assertThrows(RuntimeException.class,
                    () -> new PluginExtensionRegistry().unregister(pluginData));
            serviceListener.verify(() -> ServiceListener.removeVariableProviders(Collections.emptySet()));
            serviceListener.verify(() -> ServiceListener.removeRequestHandler(firstHandler));
            serviceListener.verify(() -> ServiceListener.removeRequestHandler(secondHandler));
        }

        assertTrue(thrown == firstFailure || thrown == secondFailure);
        assertEquals(1, thrown.getSuppressed().length);
        assertTrue(thrown.getSuppressed()[0] == firstFailure || thrown.getSuppressed()[0] == secondFailure);
        verify(firstRegistration).remove();
        verify(secondRegistration).remove();
    }
}
