package org.pacos.core.component.plugin.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;
import org.pacos.core.component.plugin.service.PluginService;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PluginManagerTest {

    private PluginService pluginService;
    private ApplicationContext applicationContext;
    private SwaggerUIConfigReload swaggerUIConfigReload;
    @TempDir
    private Path tempDir;

    @BeforeEach
    public void setUp() {
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
        PluginManager manager = new PluginManager(pluginService, swaggerUIConfigReload, applicationContext);
        //when
        manager.initializePluginsOnApplicationReadyEvent();
        //then
        assertEquals(PluginStatusEnum.OFF, PluginState.getState(pluginDTO));
    }

    @Test
    void whenAddNewPluginThenStateIsSet() {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.0");
        PluginManager manager = new PluginManager(pluginService, swaggerUIConfigReload, applicationContext);
        //when
        manager.addPlugin(pluginDTO);
        //then
        assertEquals(PluginStatusEnum.OFF, PluginState.getState(pluginDTO));
    }

    @Test
    void whenRemovePluginThenStateIsAlsoRemoved() {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.0");
        PluginManager manager = new PluginManager(pluginService, swaggerUIConfigReload, applicationContext);
        manager.initializePluginsOnApplicationReadyEvent();
        manager.addPlugin(pluginDTO);
        assertEquals(PluginStatusEnum.OFF, PluginState.getState(pluginDTO));
        //when
        manager.removePlugin(pluginDTO);
        //then
        assertNull(PluginState.getState(pluginDTO));
    }

    @Test
    void whenCantStartPluginBecauseJarFileNotFoundThenSetStatusError() {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.0");
        PluginManager manager = new PluginManager(pluginService, swaggerUIConfigReload, applicationContext);
        manager.initializePluginsOnApplicationReadyEvent();
        manager.addPlugin(pluginDTO);
        //when
        manager.startPlugin(pluginDTO);
        //then
        assertEquals(PluginStatusEnum.ERROR, PluginState.getState(pluginDTO));
    }

    @Test
    void whenPluginIsOffThenDoNotTriggerListenersWhenRemove() {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.0");
        PluginManager manager = new PluginManager(pluginService, swaggerUIConfigReload, applicationContext);
        manager.initializePluginsOnApplicationReadyEvent();
        manager.addPlugin(pluginDTO);
        PluginState.setState(pluginDTO, PluginStatusEnum.OFF);
        //when
        manager.removePlugin(pluginDTO);
        //then
        verify(pluginService).findNotRemovedPlugin();
        verify(pluginService).findEnabledPlugin();
        verifyNoMoreInteractions(pluginService);
    }

    @Test
    void whenStartExistingPluginThenSetStatusToONAndWhenPluginIsStopThenSetStatusToOFF() throws IOException {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.0");
        PluginManager manager = new PluginManager(pluginService, swaggerUIConfigReload, applicationContext);
        manager.initializePluginsOnApplicationReadyEvent();
        manager.addPlugin(pluginDTO);
        try {
            Path libDir = tempDir.resolve("lib/org/pacos/test/1.0");
            Files.createDirectories(libDir);
            File pluginJar = new File(getClass().getResource("/plugin/test-jar-without-metainf.jar").getFile());
            Files.copy(pluginJar.toPath(), libDir.resolve("test-1.0.jar"));

            //when
            manager.startPlugin(pluginDTO);
            //then
            assertEquals(PluginStatusEnum.ON, PluginState.getState(pluginDTO));
        } finally {
            manager.stopPlugin(pluginDTO);
            assertEquals(PluginStatusEnum.OFF, PluginState.getState(pluginDTO));
        }
    }

    @Test
    void whenStartTheSamePluginAgainThenReturnTrue() throws IOException, ExecutionException, InterruptedException {
        PluginDTO pluginDTO = createPlugin("test", "org.pacos", "1.0");
        PluginManager manager = new PluginManager(pluginService, swaggerUIConfigReload, applicationContext);
        manager.initializePluginsOnApplicationReadyEvent();
        manager.addPlugin(pluginDTO);
        try {
            Path libDir = tempDir.resolve("lib/org/pacos/test/1.0");
            Files.createDirectories(libDir);
            File pluginJar = new File(getClass().getResource("/plugin/test-jar-without-metainf.jar").getFile());
            Files.copy(pluginJar.toPath(), libDir.resolve("test-1.0.jar"));
            manager.startPlugin(pluginDTO);
            //when
            CompletableFuture<Boolean> completableFuture = manager.startPlugin(pluginDTO);
            //then
            assertTrue(completableFuture.get());
        } finally {
            manager.stopPlugin(pluginDTO);
        }
    }


    private PluginDTO createPlugin(String name, String group, String version) {
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setArtifactName(name);
        pluginDTO.setGroupId(group);
        pluginDTO.setVersion(version);
        return pluginDTO;
    }
}