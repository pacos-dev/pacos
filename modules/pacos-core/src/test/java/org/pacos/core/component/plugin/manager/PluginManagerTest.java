package org.pacos.core.component.plugin.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.listener.PluginListener;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.data.PluginDataLoader;
import org.pacos.core.component.plugin.manager.data.PluginJar;
import org.pacos.core.component.plugin.manager.data.RequestHandlerRegistration;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;
import org.pacos.core.component.plugin.service.PluginService;
import org.pacos.core.component.session.service.ServiceListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.vaadin.addons.variablefield.provider.VariableProvider;

import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;

class PluginManagerTest {

    private PluginService pluginService;
    private ApplicationContext applicationContext;
    private SwaggerUIConfigReload swaggerUIConfigReload;
    private PluginState pluginState;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        pluginState = new PluginState();
        pluginService = mock(PluginService.class);
        applicationContext = mock(ApplicationContext.class);
        swaggerUIConfigReload = mock(SwaggerUIConfigReload.class);
        when(pluginService.findEnabledPlugin()).thenReturn(List.of());
        when(pluginService.findNotRemovedPlugin()).thenReturn(List.of());
        System.setProperty("workingDir", tempDir.toString());
    }

    @Test
    void whenInitializeApplicationThenCreateStateOFFForDisabledPlugins() {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.0");
        when(pluginService.findNotRemovedPlugin()).thenReturn(List.of(pluginDTO));
        PluginManager manager = new PluginManager(pluginService, swaggerUIConfigReload, applicationContext, pluginState);

        manager.initializePluginsOnApplicationReadyEvent();

        assertEquals(PluginStatusEnum.OFF, pluginState.getState(pluginDTO));
    }

    @Test
    void whenAddNewPluginThenStateIsSet() {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.1");
        PluginManager manager = new PluginManager(pluginService, swaggerUIConfigReload, applicationContext, pluginState);

        manager.addPlugin(pluginDTO);

        assertEquals(PluginStatusEnum.OFF, pluginState.getState(pluginDTO));
    }

    @Test
    void whenRemovePluginThenStateIsAlsoRemoved() {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.2");
        PluginManager manager = createInitializedManager();
        manager.addPlugin(pluginDTO);

        manager.removePlugin(pluginDTO);

        assertNull(pluginState.getState(pluginDTO));
    }

    @Test
    void whenRemoveActivePluginWithoutResourcesThenStateIsRemoved() {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.3");
        PluginManager manager = createInitializedManager();
        manager.addPlugin(pluginDTO);
        pluginState.setState(pluginDTO, PluginStatusEnum.ON);

        manager.removePlugin(pluginDTO);

        assertNull(pluginState.getState(pluginDTO));
    }

    @Test
    void whenRemovingPluginResourceFailsThenStillCloseContextAndClearState() throws Exception {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.31");
        PluginManager manager = createInitializedManager();
        manager.addPlugin(pluginDTO);
        pluginState.setState(pluginDTO, PluginStatusEnum.ON);

        PluginResource pluginResource = mock(PluginResource.class);
        PluginDataLoader pluginData = mock(PluginDataLoader.class);
        when(pluginResource.get(pluginDTO)).thenReturn(pluginData);
        doThrow(new IllegalStateException("resource removal failed")).when(pluginResource).remove(pluginDTO);
        setPluginResource(manager, pluginResource);

        assertThrows(RuntimeException.class, () -> manager.removePlugin(pluginDTO));

        verify(pluginData).close();
        assertNull(pluginState.getState(pluginDTO));
    }

    @Test
    void whenCantStartPluginBecauseJarFileNotFoundThenSetStatusErrorAndReturnFalse() throws Exception {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.4");
        PluginManager manager = createInitializedManager();
        manager.addPlugin(pluginDTO);

        CompletableFuture<Boolean> result = manager.startPlugin(pluginDTO);

        assertFalse(result.get());
        assertEquals(PluginStatusEnum.ERROR, pluginState.getState(pluginDTO));
    }

    @Test
    void whenStartPluginThatCannotRunThenReturnTrueWithoutChangingState() throws Exception {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.5");
        PluginManager manager = createInitializedManager();
        manager.addPlugin(pluginDTO);
        pluginState.setState(pluginDTO, PluginStatusEnum.ON);

        CompletableFuture<Boolean> result = manager.startPlugin(pluginDTO);

        assertTrue(result.get());
        assertEquals(PluginStatusEnum.ON, pluginState.getState(pluginDTO));
    }

    @Test
    void whenStopPluginThatCannotStopThenReturnTrueWithoutChangingState() throws Exception {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.6");
        PluginManager manager = createInitializedManager();
        manager.addPlugin(pluginDTO);

        CompletableFuture<Boolean> result = manager.stopPlugin(pluginDTO);

        assertTrue(result.get());
        assertEquals(PluginStatusEnum.OFF, pluginState.getState(pluginDTO));
    }

    @Test
    void whenStopPluginWithMissingDataThenSetStatusOffAndReturnTrue() throws Exception {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.7");
        PluginManager manager = createInitializedManager();
        manager.addPlugin(pluginDTO);
        pluginState.setState(pluginDTO, PluginStatusEnum.ON);

        CompletableFuture<Boolean> result = manager.stopPlugin(pluginDTO);

        assertTrue(result.get());
        assertEquals(PluginStatusEnum.OFF, pluginState.getState(pluginDTO));
        verify(swaggerUIConfigReload).removeConfiguration(pluginDTO);
    }

    @Test
    void whenStopPluginStatusNotificationFailsThenStillCloseResourcesAndReturnFalse() throws Exception {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.79");
        PluginManager manager = createInitializedManager();
        manager.addPlugin(pluginDTO);
        ConfigurableApplicationContext pluginContext = mock(ConfigurableApplicationContext.class);
        stubEmptyPluginContext(pluginContext);
        PluginJar pluginJar = mock(PluginJar.class);
        when(pluginJar.getLibPath()).thenReturn(Path.of("/"));
        getPluginResource(manager).add(pluginDTO, pluginContext, pluginJar);
        pluginState.setState(pluginDTO, PluginStatusEnum.ON);
        VaadinSession session = mock(VaadinSession.class);
        doThrow(new IllegalStateException("session notification failed")).when(session).lock();
        ServiceListener.addVaadinSession(session);

        try {
            CompletableFuture<Boolean> result = manager.stopPlugin(pluginDTO);

            assertFalse(result.get());
            assertEquals(PluginStatusEnum.OFF, pluginState.getState(pluginDTO));
            assertNull(getPluginResource(manager).get(pluginDTO));
            verify(pluginContext).close();
            verify(pluginJar).closeClassLoader();
            verify(swaggerUIConfigReload).removeConfiguration(pluginDTO);
        } finally {
            removeVaadinSession(session);
        }
    }

    @Test
    void whenStopPluginAndContextCloseFailsThenSetStatusOffAndReturnFalse() throws Exception {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.8");
        PluginManager manager = createInitializedManager();
        manager.addPlugin(pluginDTO);
        ConfigurableApplicationContext pluginContext = mock(ConfigurableApplicationContext.class);
        stubEmptyPluginContext(pluginContext);
        doThrow(new IllegalStateException()).when(pluginContext).close();
        PluginJar pluginJar = mock(PluginJar.class);
        when(pluginJar.getLibPath()).thenReturn(Path.of("/"));
        getPluginResource(manager).add(pluginDTO, pluginContext, pluginJar);
        pluginState.setState(pluginDTO, PluginStatusEnum.ON);

        CompletableFuture<Boolean> result = manager.stopPlugin(pluginDTO);

        assertFalse(result.get());
        assertEquals(PluginStatusEnum.OFF, pluginState.getState(pluginDTO));
        assertNull(getPluginResource(manager).get(pluginDTO));
        verify(pluginJar).closeClassLoader();
        verify(swaggerUIConfigReload).removeConfiguration(pluginDTO);
    }

    @Test
    void whenStopPluginAndSwaggerRemovalFailsThenContinueCleanupAndReturnFalse() throws Exception {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.82");
        PluginManager manager = createInitializedManager();
        manager.addPlugin(pluginDTO);
        ConfigurableApplicationContext pluginContext = mock(ConfigurableApplicationContext.class);
        stubEmptyPluginContext(pluginContext);
        PluginJar pluginJar = mock(PluginJar.class);
        when(pluginJar.getLibPath()).thenReturn(Path.of("/"));
        getPluginResource(manager).add(pluginDTO, pluginContext, pluginJar);
        pluginState.setState(pluginDTO, PluginStatusEnum.ON);
        doThrow(new IllegalStateException("swagger cleanup failed"))
                .when(swaggerUIConfigReload).removeConfiguration(pluginDTO);

        CompletableFuture<Boolean> result = manager.stopPlugin(pluginDTO);

        assertFalse(result.get());
        assertEquals(PluginStatusEnum.OFF, pluginState.getState(pluginDTO));
        assertNull(getPluginResource(manager).get(pluginDTO));
        verify(pluginContext).close();
        verify(pluginJar).closeClassLoader();
        verify(swaggerUIConfigReload).removeConfiguration(pluginDTO);
    }

    @Test
    void whenStopPluginAndHandlerRegistrationRemovalFailsThenCleanupContinuesAndReturnFalse() throws Exception {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.81");
        PluginManager manager = createInitializedManager();
        manager.addPlugin(pluginDTO);
        ConfigurableApplicationContext pluginContext = mock(ConfigurableApplicationContext.class);
        stubEmptyPluginContext(pluginContext);
        PluginJar pluginJar = mock(PluginJar.class);
        when(pluginJar.getLibPath()).thenReturn(Path.of("/"));
        PluginDataLoader pluginData = getPluginResource(manager).add(pluginDTO, pluginContext, pluginJar);
        Registration registration = mock(Registration.class);
        Registration anotherRegistration = mock(Registration.class);
        doThrow(new IllegalStateException()).when(registration).remove();
        doThrow(new IllegalArgumentException()).when(anotherRegistration).remove();
        pluginData.addRequestHandlerRegistration(
                new RequestHandlerRegistration(registration, mock(RequestHandler.class)));
        pluginData.addRequestHandlerRegistration(
                new RequestHandlerRegistration(anotherRegistration, mock(RequestHandler.class)));
        pluginState.setState(pluginDTO, PluginStatusEnum.ON);

        CompletableFuture<Boolean> result = manager.stopPlugin(pluginDTO);

        assertFalse(result.get());
        assertEquals(PluginStatusEnum.OFF, pluginState.getState(pluginDTO));
        assertNull(getPluginResource(manager).get(pluginDTO));
        verify(pluginContext).close();
        verify(pluginJar).closeClassLoader();
    }

    @Test
    void whenStartPluginAndResourceRegistrationFailsThenCloseContextAndMarkError() throws Exception {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.0");
        PluginManager manager = createInitializedManager();
        manager.addPlugin(pluginDTO);
        copyPluginJar("1.0");
        PluginResource pluginResource = mock(PluginResource.class);
        setPluginResource(manager, pluginResource);
        when(pluginResource.add(eq(pluginDTO), any(ApplicationContext.class), any(PluginJar.class)))
                .thenThrow(new IllegalStateException());

        CompletableFuture<Boolean> result = manager.startPlugin(pluginDTO);

        assertFalse(result.get());
        assertEquals(PluginStatusEnum.ERROR, pluginState.getState(pluginDTO));
        verify(pluginResource).add(eq(pluginDTO), any(ApplicationContext.class), any(PluginJar.class));
    }

    @Test
    void whenPluginIsOffThenDoNotTriggerListenersWhenRemove() {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.9");
        PluginManager manager = createInitializedManager();
        manager.addPlugin(pluginDTO);
        pluginState.setState(pluginDTO, PluginStatusEnum.OFF);

        manager.removePlugin(pluginDTO);

        verify(pluginService).findNotRemovedPlugin();
        verify(pluginService).findEnabledPlugin();
        assertNull(pluginState.getState(pluginDTO));
    }

    @Test
    void whenStartExistingPluginThenSetStatusToONAndWhenPluginIsStopThenSetStatusToOFF() throws Exception {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.0");
        PluginManager manager = createInitializedManager();
        manager.addPlugin(pluginDTO);
        copyPluginJar("1.0");

        try {
            assertTrue(manager.startPlugin(pluginDTO).get());
            assertEquals(PluginStatusEnum.ON, pluginState.getState(pluginDTO));
        } finally {
            manager.stopPlugin(pluginDTO);
            assertEquals(PluginStatusEnum.OFF, pluginState.getState(pluginDTO));
            manager.removePlugin(pluginDTO);
        }
    }

    @Test
    void whenRemoveRunningPluginThenCloseResourcesAndRemoveState() throws Exception {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.0");
        PluginManager manager = createInitializedManager();
        manager.addPlugin(pluginDTO);
        copyPluginJar("1.0");

        assertTrue(manager.startPlugin(pluginDTO).get());

        manager.removePlugin(pluginDTO);

        assertNull(pluginState.getState(pluginDTO));
        assertNull(getPluginResource(manager).get(pluginDTO));
    }

    @Test
    void whenStartPluginButSwaggerConfigurationFailsThenCleanupAndReturnFalse() throws Exception {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.0");
        PluginManager manager = createInitializedManager();
        manager.addPlugin(pluginDTO);
        copyPluginJar("1.0");
        doThrow(new IllegalStateException()).when(swaggerUIConfigReload).addConfiguration(pluginDTO);

        CompletableFuture<Boolean> result = manager.startPlugin(pluginDTO);

        assertFalse(result.get());
        assertEquals(PluginStatusEnum.ERROR, pluginState.getState(pluginDTO));
        assertNull(getPluginResource(manager).get(pluginDTO));
        manager.removePlugin(pluginDTO);
    }

    @Test
    void whenStartTheSamePluginAgainThenReturnTrue() throws Exception {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.0");
        PluginManager manager = createInitializedManager();
        manager.addPlugin(pluginDTO);
        copyPluginJar("1.0");

        try {
            assertTrue(manager.startPlugin(pluginDTO).get());
            CompletableFuture<Boolean> result = manager.startPlugin(pluginDTO);

            assertTrue(result.get());
        } finally {
            manager.stopPlugin(pluginDTO);
            manager.removePlugin(pluginDTO);
        }
    }

    private PluginManager createInitializedManager() {
        PluginManager manager = new PluginManager(pluginService, swaggerUIConfigReload, applicationContext, pluginState);
        manager.initializePluginsOnApplicationReadyEvent();
        return manager;
    }

    private void copyPluginJar(String version) throws IOException {
        Path libDir = tempDir.resolve("lib/org/pacos/test/" + version);
        Files.createDirectories(libDir);
        File pluginJar = new File(getClass().getResource("/plugin/test-jar-without-metainf.jar").getFile());
        Files.copy(pluginJar.toPath(), libDir.resolve("test-" + version + ".jar"));
    }

    private PluginResource getPluginResource(PluginManager manager) throws ReflectiveOperationException {
        Field field = PluginManager.class.getDeclaredField("pluginResource");
        field.setAccessible(true);
        return (PluginResource) field.get(manager);
    }

    private void setPluginResource(PluginManager manager, PluginResource pluginResource)
            throws ReflectiveOperationException {
        Field field = PluginManager.class.getDeclaredField("pluginResource");
        field.setAccessible(true);
        field.set(manager, pluginResource);
    }

    @SuppressWarnings("unchecked")
    private void removeVaadinSession(VaadinSession session) throws ReflectiveOperationException {
        Field field = ServiceListener.class.getDeclaredField("allSessions");
        field.setAccessible(true);
        ((Set<VaadinSession>) field.get(null)).remove(session);
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

    private PluginDTO createPlugin(String name, String group, String version) {
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setArtifactName(name);
        pluginDTO.setGroupId(group);
        pluginDTO.setVersion(version);
        return pluginDTO;
    }
}
