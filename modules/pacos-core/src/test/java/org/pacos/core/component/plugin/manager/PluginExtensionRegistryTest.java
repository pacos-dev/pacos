package org.pacos.core.component.plugin.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.plugin.manager.data.PluginDataLoader;
import org.pacos.core.component.plugin.manager.data.RequestHandlerRegistration;

import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.shared.Registration;

class PluginExtensionRegistryTest {

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

        new PluginExtensionRegistry().unregister(pluginData);

        verify(firstRegistration).remove();
        verify(secondRegistration).remove();
    }

    @Test
    void whenSeveralCleanupOperationsFailThenThrowFirstFailureAndSuppressTheRest() {
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

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> new PluginExtensionRegistry().unregister(pluginData));

        assertEquals(firstFailure, thrown);
        assertTrue(java.util.Arrays.asList(thrown.getSuppressed()).contains(firstFailure)
                || thrown.getSuppressed().length >= 1);
        verify(firstRegistration).remove();
        verify(secondRegistration).remove();
    }
}
