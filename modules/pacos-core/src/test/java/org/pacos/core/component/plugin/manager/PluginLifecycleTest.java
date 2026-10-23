package org.pacos.core.component.plugin.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.vaadin.addons.variablefield.provider.VariableProvider;

import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.shared.Registration;

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

        assertSame(failure, assertThrows(RuntimeException.class, () -> lifecycle.removePlugin(pluginDTO, () -> {
            verify(pluginData).close();
            assertEquals(PluginStatusEnum.ON, pluginState.getState(pluginDTO));
            pluginState.removePlugin(pluginDTO);
        })));

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

    private void copyPluginJar(String version) throws IOException {
        Path libDir = tempDir.resolve("lib/org/pacos/test/" + version);
        Files.createDirectories(libDir);
        File pluginJar = new File(getClass().getResource("/plugin/test-jar-without-metainf.jar").getFile());
        Files.copy(pluginJar.toPath(), libDir.resolve("test-" + version + ".jar"));
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
