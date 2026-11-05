package org.pacos.core.component.plugin.manager;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.listener.PluginListener;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.data.PluginDataLoader;
import org.pacos.core.component.plugin.manager.data.PluginJar;
import org.pacos.core.component.plugin.manager.data.PluginStatus;
import org.pacos.core.component.plugin.manager.data.RequestHandlerRegistration;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;
import org.pacos.core.component.session.service.ServiceListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.vaadin.addons.variablefield.provider.VariableProvider;

import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.shared.Registration;

@Isolated("Shares JVM-wide system properties or other global test resources; keep isolated during parallel runs.")
class PluginLifecycleTest {

    private ApplicationContext applicationContext;
    private SwaggerUIConfigReload swaggerUIConfigReload;
    private PluginState pluginState;
    private PluginLifecycle lifecycle;
    private PluginResource pluginResource;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        pluginState = new PluginState();
        applicationContext = mock(ApplicationContext.class);
        swaggerUIConfigReload = mock(SwaggerUIConfigReload.class);
        System.setProperty("workingDir", tempDir.toString());
        lifecycle = new PluginLifecycle(swaggerUIConfigReload, applicationContext, pluginState);
        lifecycle.initializeResources();
        pluginResource = (PluginResource) ReflectionTestUtils.getField(lifecycle, "pluginResource");
    }

    @Test
    void whenCallRemoveActivePluginWithoutResourcesThenRemoveState() {
        PluginDTO pluginDTO = createPlugin("1.3");
        pluginState.addPlugin(pluginDTO);
        pluginState.setState(pluginDTO, PluginStatusEnum.ON);

        lifecycle.removePlugin(pluginDTO, () -> pluginState.removePlugin(pluginDTO));

        assertNull(pluginState.getState(pluginDTO));
    }

    @Test
    void whenCallRemovePluginAndResourceRemovalFailsThenCloseContextAndRemoveStateBeforeRethrowing() {
        PluginDTO pluginDTO = createPlugin("1.31");
        pluginState.addPlugin(pluginDTO);
        pluginState.setState(pluginDTO, PluginStatusEnum.ON);
        pluginResource = mock(PluginResource.class);
        PluginDataLoader pluginData = mock(PluginDataLoader.class);
        when(pluginResource.get(pluginDTO)).thenReturn(pluginData);
        IllegalStateException failure = new IllegalStateException("resource removal failed");
        doThrow(failure).when(pluginResource).remove(pluginDTO);
        ReflectionTestUtils.setField(lifecycle, "pluginResource", pluginResource);
        Runnable removeFromCatalog = () -> pluginState.removePlugin(pluginDTO);

        assertEquals(PluginStatusEnum.ON, pluginState.getState(pluginDTO));
        assertSame(failure, assertThrows(RuntimeException.class,
                () -> lifecycle.removePlugin(pluginDTO, removeFromCatalog)));

        verify(pluginData).close();
        assertNull(pluginState.getState(pluginDTO));
    }

    @Test
    void whenCallStartPluginWithMissingJarThenSetStatusErrorAndReturnFalse() throws Exception {
        PluginDTO pluginDTO = createPlugin("1.4");
        pluginState.addPlugin(pluginDTO);

        CompletableFuture<Boolean> result = lifecycle.startPlugin(pluginDTO);

        assertFalse(result.get());
        assertEquals(PluginStatusEnum.ERROR, pluginState.getState(pluginDTO));
    }

    @Test
    void whenCallStartPluginThatCannotRunThenReturnTrueWithoutChangingState() throws Exception {
        PluginDTO pluginDTO = createPlugin("1.5");
        pluginState.addPlugin(pluginDTO);
        pluginState.setState(pluginDTO, PluginStatusEnum.ON);

        CompletableFuture<Boolean> result = lifecycle.startPlugin(pluginDTO);

        assertTrue(result.get());
        assertEquals(PluginStatusEnum.ON, pluginState.getState(pluginDTO));
    }

    @Test
    void whenCallStopPluginThatCannotStopThenReturnTrueWithoutChangingState() throws Exception {
        PluginDTO pluginDTO = createPlugin("1.6");
        pluginState.addPlugin(pluginDTO);

        CompletableFuture<Boolean> result = lifecycle.stopPlugin(pluginDTO);

        assertTrue(result.get());
        assertEquals(PluginStatusEnum.OFF, pluginState.getState(pluginDTO));
    }

    @Test
    void whenCallStopPluginWithMissingDataThenSetStatusOffAndReturnTrue() throws Exception {
        PluginDTO pluginDTO = createPlugin("1.7");
        pluginState.addPlugin(pluginDTO);
        pluginState.setState(pluginDTO, PluginStatusEnum.ON);

        CompletableFuture<Boolean> result = lifecycle.stopPlugin(pluginDTO);

        assertTrue(result.get());
        assertEquals(PluginStatusEnum.OFF, pluginState.getState(pluginDTO));
        verify(swaggerUIConfigReload).removeConfiguration(pluginDTO);
    }

    @Test
    void whenCallStopPluginAndContextCloseFailsThenSetStatusOffAndReturnFalse() throws Exception {
        PluginDTO pluginDTO = createPlugin("1.8");
        pluginState.addPlugin(pluginDTO);
        ConfigurableApplicationContext pluginContext = mock(ConfigurableApplicationContext.class);
        stubEmptyPluginContext(pluginContext);
        doThrow(new IllegalStateException()).when(pluginContext).close();
        PluginJar pluginJar = mock(PluginJar.class);
        when(pluginJar.getLibPath()).thenReturn(Path.of("/"));
        pluginResource.add(pluginDTO, pluginContext, pluginJar);
        pluginState.setState(pluginDTO, PluginStatusEnum.ON);

        CompletableFuture<Boolean> result = lifecycle.stopPlugin(pluginDTO);

        assertFalse(result.get());
        assertEquals(PluginStatusEnum.OFF, pluginState.getState(pluginDTO));
        assertNull(pluginResource.get(pluginDTO));
        verify(pluginJar).closeClassLoader();
        verify(swaggerUIConfigReload).removeConfiguration(pluginDTO);
    }

    @Test
    void whenCallStopPluginAndHandlerRegistrationRemovalFailsThenContinueCleanupAndReturnFalse() throws Exception {
        PluginDTO pluginDTO = createPlugin("1.81");
        pluginState.addPlugin(pluginDTO);
        ConfigurableApplicationContext pluginContext = mock(ConfigurableApplicationContext.class);
        stubEmptyPluginContext(pluginContext);
        PluginJar pluginJar = mock(PluginJar.class);
        when(pluginJar.getLibPath()).thenReturn(Path.of("/"));
        PluginDataLoader pluginData = pluginResource.add(pluginDTO, pluginContext, pluginJar);
        Registration registration = mock(Registration.class);
        Registration anotherRegistration = mock(Registration.class);
        doThrow(new IllegalStateException()).when(registration).remove();
        doThrow(new IllegalArgumentException()).when(anotherRegistration).remove();
        pluginData.addRequestHandlerRegistration(
                new RequestHandlerRegistration(registration, mock(RequestHandler.class)));
        pluginData.addRequestHandlerRegistration(
                new RequestHandlerRegistration(anotherRegistration, mock(RequestHandler.class)));
        pluginState.setState(pluginDTO, PluginStatusEnum.ON);

        CompletableFuture<Boolean> result = lifecycle.stopPlugin(pluginDTO);

        assertFalse(result.get());
        assertEquals(PluginStatusEnum.OFF, pluginState.getState(pluginDTO));
        assertNull(pluginResource.get(pluginDTO));
        verify(pluginContext).close();
        verify(pluginJar).closeClassLoader();
    }

    @Test
    void whenCallStartPluginAndResourceRegistrationFailsThenCloseContextAndMarkError() throws Exception {
        PluginDTO pluginDTO = createPlugin("1.0");
        pluginState.addPlugin(pluginDTO);
        copyPluginJar("1.0");
        pluginResource = mock(PluginResource.class);
        ReflectionTestUtils.setField(lifecycle, "pluginResource", pluginResource);
        when(pluginResource.add(eq(pluginDTO), any(ApplicationContext.class), any(PluginJar.class)))
                .thenThrow(new IllegalStateException());

        CompletableFuture<Boolean> result = lifecycle.startPlugin(pluginDTO);

        assertFalse(result.get());
        assertEquals(PluginStatusEnum.ERROR, pluginState.getState(pluginDTO));
        verify(pluginResource).add(eq(pluginDTO), any(ApplicationContext.class), any(PluginJar.class));
    }

    @Test
    void whenCallRemoveOffPluginThenDoNotNotifyListeners() {
        PluginListener listener = mock(PluginListener.class);
        when(applicationContext.getBeansOfType(PluginListener.class)).thenReturn(Map.of("listener", listener));
        lifecycle.initializeResources();
        PluginDTO pluginDTO = createPlugin("1.9");
        pluginState.addPlugin(pluginDTO);
        pluginState.setState(pluginDTO, PluginStatusEnum.OFF);

        lifecycle.removePlugin(pluginDTO, () -> pluginState.removePlugin(pluginDTO));

        verifyNoInteractions(listener);
        assertNull(pluginState.getState(pluginDTO));
    }

    @Test
    void whenCallStartAndStopExistingPluginThenSetStatusOnAndOff() throws Exception {
        PluginDTO pluginDTO = createPlugin("1.0");
        pluginState.addPlugin(pluginDTO);
        copyPluginJar("1.0");

        try {
            assertTrue(lifecycle.startPlugin(pluginDTO).get());
            assertEquals(PluginStatusEnum.ON, pluginState.getState(pluginDTO));
        } finally {
            lifecycle.stopPlugin(pluginDTO);
            assertEquals(PluginStatusEnum.OFF, pluginState.getState(pluginDTO));
            lifecycle.removePlugin(pluginDTO, () -> pluginState.removePlugin(pluginDTO));
        }
    }

    @Test
    void whenCallRemoveRunningPluginThenCloseResourcesAndRemoveState() throws Exception {
        PluginDTO pluginDTO = createPlugin("1.0");
        pluginState.addPlugin(pluginDTO);
        copyPluginJar("1.0");

        assertTrue(lifecycle.startPlugin(pluginDTO).get());

        lifecycle.removePlugin(pluginDTO, () -> pluginState.removePlugin(pluginDTO));

        assertNull(pluginState.getState(pluginDTO));
        assertNull(pluginResource.get(pluginDTO));
    }

    @Test
    void whenCallStartPluginAndSwaggerConfigurationFailsThenCleanupAndReturnFalse() throws Exception {
        PluginDTO pluginDTO = createPlugin("1.0");
        pluginState.addPlugin(pluginDTO);
        copyPluginJar("1.0");
        doThrow(new IllegalStateException()).when(swaggerUIConfigReload).addConfiguration(pluginDTO);

        CompletableFuture<Boolean> result = lifecycle.startPlugin(pluginDTO);

        assertFalse(result.get());
        assertEquals(PluginStatusEnum.ERROR, pluginState.getState(pluginDTO));
        assertNull(pluginResource.get(pluginDTO));
        lifecycle.removePlugin(pluginDTO, () -> pluginState.removePlugin(pluginDTO));
    }

    @Test
    void whenCallStartTheSamePluginAgainThenReturnTrue() throws Exception {
        PluginDTO pluginDTO = createPlugin("1.0");
        pluginState.addPlugin(pluginDTO);
        copyPluginJar("1.0");

        try {
            assertTrue(lifecycle.startPlugin(pluginDTO).get());
            CompletableFuture<Boolean> result = lifecycle.startPlugin(pluginDTO);

            assertTrue(result.get());
        } finally {
            lifecycle.stopPlugin(pluginDTO);
            lifecycle.removePlugin(pluginDTO, () -> pluginState.removePlugin(pluginDTO));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "true, false, false",
            "false, true, false",
            "false, false, true",
            "true, true, true"
    })
    void whenCallRemovePluginAndCleanupFailsThenRemoveCatalogAndRethrowCombinedFailures(
            boolean unregisterFails, boolean removeFails, boolean closeFails) {
        PluginDTO plugin = createPlugin("1.0");
        pluginState.addPlugin(plugin);
        PluginDataLoader data = mockPluginResources(plugin);
        PluginExtensionRegistry registry = mockExtensionRegistry();
        RuntimeException unregisterFailure = new IllegalStateException("unregister failed");
        RuntimeException removeFailure = new IllegalStateException("remove failed");
        RuntimeException closeFailure = new IllegalStateException("close failed");
        if (unregisterFails) {
            doThrow(unregisterFailure).when(registry).unregister(data);
        }
        if (removeFails) {
            doThrow(removeFailure).when(pluginResource).remove(plugin);
        }
        if (closeFails) {
            doThrow(closeFailure).when(data).close();
        }
        Runnable removeFromCatalog = mock(Runnable.class);

        RuntimeException failure = assertThrows(RuntimeException.class,
                () -> lifecycle.removePlugin(plugin, removeFromCatalog));

        assertSame(unregisterFails ? unregisterFailure : removeFails ? removeFailure : closeFailure, failure);
        assertArrayEquals(unregisterFails && removeFails && closeFails
                ? new Throwable[] { removeFailure, closeFailure } : new Throwable[0], failure.getSuppressed());
        var order = inOrder(registry, pluginResource, data, removeFromCatalog);
        order.verify(registry).unregister(data);
        order.verify(pluginResource).remove(plugin);
        order.verify(data).close();
        order.verify(removeFromCatalog).run();
        assertLifecycleLocksReleased();
    }

    @Test
    void whenCallRemovePluginBeforeResourcesInitializedThenRemoveCatalogAndReleaseLock() {
        lifecycle = new PluginLifecycle(swaggerUIConfigReload, applicationContext, pluginState);
        Runnable removeFromCatalog = mock(Runnable.class);

        lifecycle.removePlugin(createPlugin("1.0"), removeFromCatalog);

        verify(removeFromCatalog).run();
        assertLifecycleLocksReleased();
    }

    @Test
    void whenCallRemovePluginAndCatalogRemovalFailsThenPropagateFailureAndReleaseLock() {
        RuntimeException failure = new IllegalStateException("catalog removal failed");
        Runnable removeFromCatalog = mock(Runnable.class);
        doThrow(failure).when(removeFromCatalog).run();
        PluginDTO plugin = createPlugin("1.0");

        assertSame(failure, assertThrows(RuntimeException.class,
                () -> lifecycle.removePlugin(plugin, removeFromCatalog)));

        assertLifecycleLocksReleased();
    }

    @ParameterizedTest
    @CsvSource({
            "true, false, false, false",
            "false, true, false, false",
            "false, false, true, false",
            "false, false, false, true",
            "true, true, true, true"
    })
    void whenCallStopPluginAndCleanupFailsThenFinishAllCleanupAndNotifyUninstallation(
            boolean notifyFails, boolean unregisterFails, boolean removeFails, boolean closeFails) {
        PluginDTO plugin = createPlugin("1.0");
        pluginState.addPlugin(plugin);
        pluginState.setState(plugin, PluginStatusEnum.ON);
        PluginDataLoader data = mockPluginResources(plugin);
        PluginExtensionRegistry registry = mockExtensionRegistry();
        if (notifyFails) {
            doThrow(new IllegalStateException("notification failed")).when(registry).notifyWindowsRemoved(data);
        }
        if (unregisterFails) {
            doThrow(new IllegalStateException("unregister failed")).when(registry).unregister(data);
        }
        if (removeFails) {
            doThrow(new IllegalStateException("remove failed")).when(pluginResource).remove(plugin);
        }
        if (closeFails) {
            doThrow(new IllegalStateException("close failed")).when(data).close();
        }

        try (var events = mockStatic(ServiceListener.class)) {
            assertFalse(lifecycle.stopPlugin(plugin).join());

            var order = inOrder(registry, pluginResource, data, swaggerUIConfigReload);
            order.verify(registry).notifyWindowsRemoved(data);
            order.verify(registry).unregister(data);
            order.verify(pluginResource).remove(plugin);
            order.verify(data).close();
            order.verify(swaggerUIConfigReload).removeConfiguration(plugin);
            assertEquals(PluginStatusEnum.OFF, pluginState.getState(plugin));
            events.verify(() -> ServiceListener.notifyAll(ModuleEvent.PLUGIN_INSTALL_STATE_CHANGED,
                    new PluginStatus(plugin, PluginStatusEnum.SHUTDOWN)));
            events.verify(() -> ServiceListener.notifyAll(ModuleEvent.PLUGIN_INSTALL_STATE_CHANGED,
                    new PluginStatus(plugin, PluginStatusEnum.OFF)));
            events.verify(() -> ServiceListener.notifyAll(ModuleEvent.PLUGIN_UNINSTALLED, plugin));
        }
        assertLifecycleLocksReleased();
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void whenCallStartPluginAndRegistrationFailsThenSuppressCleanupFailuresAndMarkError(boolean extensionFails) {
        PluginDTO plugin = createPlugin("1.0");
        pluginState.addPlugin(plugin);
        PluginDataLoader data = mockPluginResources(plugin);
        PluginExtensionRegistry registry = mockExtensionRegistry();
        RuntimeException failure = new IllegalStateException("registration failed");
        RuntimeException unregisterFailure = new IllegalStateException("unregister failed");
        RuntimeException removeFailure = new IllegalStateException("remove failed");
        RuntimeException closeFailure = new IllegalStateException("close failed");
        doThrow(unregisterFailure).when(registry).unregister(data);
        doThrow(removeFailure).when(pluginResource).remove(plugin);
        doThrow(closeFailure).when(data).close();
        if (extensionFails) {
            doThrow(failure).when(registry).register(data);
        } else {
            doThrow(failure).when(swaggerUIConfigReload).addConfiguration(plugin);
        }
        when(pluginResource.add(eq(plugin), any(ApplicationContext.class), any(PluginJar.class))).thenReturn(data);

        try (var jars = mockConstruction(PluginJar.class, (jar, context) -> when(jar.exists()).thenReturn(true));
             var contexts = mockConstruction(AnnotationConfigApplicationContext.class);
             var events = mockStatic(ServiceListener.class)) {
            assertFalse(lifecycle.startPlugin(plugin).join());

            assertArrayEquals(new Throwable[] { unregisterFailure, removeFailure, closeFailure }, failure.getSuppressed());
            assertEquals(PluginStatusEnum.ERROR, pluginState.getState(plugin));
            var order = inOrder(contexts.constructed().getFirst(), pluginResource, registry, data);
            order.verify(contexts.constructed().getFirst()).refresh();
            order.verify(pluginResource).add(plugin, contexts.constructed().getFirst(), jars.constructed().getFirst());
            order.verify(registry).register(data);
            order.verify(registry).unregister(data);
            order.verify(pluginResource).remove(plugin);
            order.verify(data).close();
            events.verify(() -> ServiceListener.notifyAll(ModuleEvent.PLUGIN_INSTALL_STATE_CHANGED,
                    new PluginStatus(plugin, PluginStatusEnum.ERROR)));
            events.verify(() -> ServiceListener.notifyAll(ModuleEvent.PLUGIN_INSTALLED, plugin), never());
            verify(swaggerUIConfigReload, never()).removeConfiguration(plugin);
        }
        assertLifecycleLocksReleased();
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void whenCallStartPluginAndResourceAdditionFailsThenCloseContextAndClassLoader(boolean closeFails) {
        PluginDTO plugin = createPlugin("1.0");
        pluginState.addPlugin(plugin);
        mockPluginResources(plugin);
        RuntimeException failure = new IllegalStateException("resource addition failed");
        RuntimeException closeFailure = new IllegalStateException("context close failed");
        when(pluginResource.add(eq(plugin), any(ApplicationContext.class), any(PluginJar.class))).thenThrow(failure);

        try (var jars = mockConstruction(PluginJar.class, (jar, context) -> when(jar.exists()).thenReturn(true));
             var contexts = mockConstruction(AnnotationConfigApplicationContext.class, (context, construction) -> {
                 if (closeFails) {
                     doThrow(closeFailure).when(context).close();
                 }
             })) {
            assertFalse(lifecycle.startPlugin(plugin).join());

            var context = contexts.constructed().getFirst();
            var jar = jars.constructed().getFirst();
            var order = inOrder(context, jar);
            order.verify(context).close();
            order.verify(jar, times(2)).closeClassLoader();
            assertArrayEquals(closeFails ? new Throwable[] { closeFailure } : new Throwable[0], failure.getSuppressed());
            assertEquals(PluginStatusEnum.ERROR, pluginState.getState(plugin));
            verifyNoInteractions(swaggerUIConfigReload);
        }
        assertLifecycleLocksReleased();
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void whenCallStartPluginAndContextRefreshFailsThenCloseContextAndStopModuleLogger(boolean closeFails) {
        PluginDTO plugin = createPlugin("1.0");
        pluginState.addPlugin(plugin);
        RuntimeException failure = new IllegalStateException("context refresh failed");
        RuntimeException closeFailure = new IllegalStateException("context close failed");

        try (var jars = mockConstruction(PluginJar.class, (jar, context) -> when(jar.exists()).thenReturn(true));
             var loggers = mockConstruction(ModuleLogger.class, (logger, context) ->
                     when(logger.getLogger()).thenReturn(mock(ch.qos.logback.classic.Logger.class)));
             var contexts = mockConstruction(AnnotationConfigApplicationContext.class, (context, construction) -> {
                 doThrow(failure).when(context).refresh();
                 if (closeFails) {
                     doThrow(closeFailure).when(context).close();
                 }
             })) {
            assertFalse(lifecycle.startPlugin(plugin).join());

            var context = contexts.constructed().getFirst();
            var order = inOrder(context, loggers.constructed().getFirst(), jars.constructed().getFirst());
            order.verify(context).setClassLoader(null);
            order.verify(context).setParent(applicationContext);
            order.verify(context).scan("org.pacos.plugin.test.config");
            order.verify(context).registerBean(RequestMappingHandlerMapping.class);
            order.verify(context).refresh();
            order.verify(context).close();
            order.verify(loggers.constructed().getFirst()).stopLogger();
            order.verify(jars.constructed().getFirst()).closeClassLoader();
            assertArrayEquals(closeFails ? new Throwable[] { closeFailure } : new Throwable[0], failure.getSuppressed());
            assertEquals(PluginStatusEnum.ERROR, pluginState.getState(plugin));
            verifyNoInteractions(swaggerUIConfigReload);
        }
        assertLifecycleLocksReleased();
    }

    @Test
    void whenCallStartPluginAndClassLoaderCreationFailsThenCloseJarAndMarkError() {
        PluginDTO plugin = createPlugin("1.0");
        pluginState.addPlugin(plugin);

        try (var jars = mockConstruction(PluginJar.class, (jar, context) -> {
            when(jar.exists()).thenReturn(true);
            when(jar.getPluginClassLoader()).thenThrow(new MalformedURLException("invalid jar URL"));
        })) {
            assertFalse(lifecycle.startPlugin(plugin).join());

            verify(jars.constructed().getFirst()).closeClassLoader();
            assertEquals(PluginStatusEnum.ERROR, pluginState.getState(plugin));
            verifyNoInteractions(swaggerUIConfigReload);
        }
        assertLifecycleLocksReleased();
    }

    @Test
    void whenCallStartPluginAndStateCheckFailsThenMarkErrorAndReleaseLock() {
        PluginDTO plugin = createPlugin("1.0");
        PluginState state = mock(PluginState.class);
        lifecycle = new PluginLifecycle(swaggerUIConfigReload, applicationContext, state);
        when(state.canRun(plugin)).thenThrow(new IllegalStateException("state check failed"));

        assertFalse(lifecycle.startPlugin(plugin).join());

        verify(state).setState(plugin, PluginStatusEnum.ERROR);
        verifyNoInteractions(swaggerUIConfigReload);
        assertLifecycleLocksReleased();
    }

    private PluginDataLoader mockPluginResources(PluginDTO plugin) {
        pluginResource = mock(PluginResource.class);
        ReflectionTestUtils.setField(lifecycle, "pluginResource", pluginResource);
        PluginDataLoader data = mock(PluginDataLoader.class);
        when(pluginResource.get(plugin)).thenReturn(data);
        return data;
    }

    private PluginExtensionRegistry mockExtensionRegistry() {
        PluginExtensionRegistry registry = mock(PluginExtensionRegistry.class);
        ReflectionTestUtils.setField(lifecycle, "extensionRegistry", registry);
        return registry;
    }

    private void assertLifecycleLocksReleased() {
        Map<?, ?> locks = (Map<?, ?>) ReflectionTestUtils.getField(lifecycle, "lifecycleLocks");
        assertTrue(locks.isEmpty());
    }

    private void copyPluginJar(String version) throws IOException {
        Path libDir = tempDir.resolve("lib/org/pacos/test/" + version);
        Files.createDirectories(libDir);
        try (InputStream pluginJar = Objects.requireNonNull(
                getClass().getResourceAsStream("/plugin/test-jar-without-metainf.jar"),
                "Missing test resource: /plugin/test-jar-without-metainf.jar")) {
            Files.copy(pluginJar, libDir.resolve("test-" + version + ".jar"));
        }
    }

    private void stubEmptyPluginContext(ApplicationContext context) {
        when(context.getBeansOfType(WindowConfig.class)).thenReturn(Collections.emptyMap());
        when(context.getBeansOfType(SettingTab.class)).thenReturn(Collections.emptyMap());
        when(context.getBeansOfType(VariableProvider.class)).thenReturn(Collections.emptyMap());
        when(context.getBeansOfType(PluginListener.class)).thenReturn(Collections.emptyMap());
        when(context.getBeansOfType(RequestHandler.class)).thenReturn(Collections.emptyMap());
        when(context.getBean(RequestMappingInfoHandlerMapping.class)).thenReturn(mock(RequestMappingInfoHandlerMapping.class));
        when(context.getBean(RequestMappingHandlerAdapter.class)).thenReturn(mock(RequestMappingHandlerAdapter.class));
    }

    private PluginDTO createPlugin(String version) {
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setArtifactName("test");
        pluginDTO.setGroupId("org.pacos");
        pluginDTO.setVersion(version);
        return pluginDTO;
    }
}
