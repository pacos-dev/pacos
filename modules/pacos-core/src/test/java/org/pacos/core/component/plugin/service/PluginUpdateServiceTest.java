package org.pacos.core.component.plugin.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;
import org.pacos.core.component.plugin.service.data.PluginUpdateResult;
import org.pacos.core.component.plugin.service.data.PluginsToUpdate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PluginUpdateServiceTest {

    private PluginUpdateService updatePluginService;
    private PluginInstallService pluginInstallService;
    private PluginService pluginService;
    private PluginManager pluginManager;
    private PluginState pluginState;

    @BeforeEach
    void setUp() {
        pluginInstallService = mock(PluginInstallService.class);
        pluginService = mock(PluginService.class);
        pluginManager = mock(PluginManager.class);
        pluginState = new PluginState();
        updatePluginService = new PluginUpdateService(pluginInstallService, pluginManager, pluginService, pluginState);
    }

    @Test
    void whenNoPluginsToUpdateThenReturnEmptyList() {
        PluginUpdateResult result =
                updatePluginService.updatePlugins(new PluginsToUpdate(List.of(), AppRepository.pluginRepo()));

        assertTrue(result.updated().isEmpty());
        assertTrue(result.failed().isEmpty());
    }

    @Test
    void whenPluginUpdateCompletesThenReturnPluginAsUpdated() {
        PluginDTO plugin = createPlugin();
        PluginsToUpdate request = new PluginsToUpdate(List.of(plugin), AppRepository.pluginRepo());
        when(pluginService.findByArtifactNameAndGroupId(plugin.getArtifactName(), plugin.getGroupId()))
                .thenReturn(List.of());
        when(pluginManager.startPlugin(plugin)).thenReturn(CompletableFuture.completedFuture(true));

        try (MockedStatic<PluginDownloadService> download = mockStatic(PluginDownloadService.class)) {
            download.when(() -> PluginDownloadService.downloadPlugin(request.repository(), plugin.toArtifact(), plugin))
                    .thenReturn(plugin);

            PluginUpdateResult result = updatePluginService.updatePlugins(request);

            assertEquals(List.of(plugin), result.updated());
            assertTrue(result.failed().isEmpty());
            verify(pluginInstallService).savePlugin(plugin);
            verify(pluginManager).startPlugin(plugin);
        }
    }

    @Test
    void whenStoppingOldPluginFailsThenDoNotInstallNewPlugin() {
        PluginDTO plugin = createPlugin();
        PluginDTO oldPlugin = createPlugin();
        oldPlugin.setVersion("0.9");
        PluginsToUpdate request = new PluginsToUpdate(List.of(plugin), AppRepository.pluginRepo());
        pluginState.addPlugin(oldPlugin);
        pluginState.setState(oldPlugin, PluginStatusEnum.ON);
        when(pluginService.findByArtifactNameAndGroupId(plugin.getArtifactName(), plugin.getGroupId()))
                .thenReturn(List.of(oldPlugin));
        when(pluginManager.stopPlugin(oldPlugin)).thenReturn(CompletableFuture.completedFuture(false));
        when(pluginManager.startPlugin(oldPlugin)).thenReturn(CompletableFuture.completedFuture(true));

        try (MockedStatic<PluginDownloadService> download = mockStatic(PluginDownloadService.class)) {
            download.when(() -> PluginDownloadService.downloadPlugin(request.repository(), plugin.toArtifact(), plugin))
                    .thenReturn(plugin);

            PluginUpdateResult result = updatePluginService.updatePlugins(request);

            assertTrue(result.updated().isEmpty());
            assertEquals(List.of(plugin), result.failed());
            verify(pluginInstallService, never()).savePlugin(plugin);
            verify(pluginManager, never()).startPlugin(plugin);
            verify(pluginManager).startPlugin(oldPlugin);
            verify(pluginService, never()).removePlugin(oldPlugin);
        }
    }

    @Test
    void whenStartingUpdatedPluginFailsThenReportFailure() {
        PluginDTO plugin = createPlugin();
        PluginsToUpdate request = new PluginsToUpdate(List.of(plugin), AppRepository.pluginRepo());
        when(pluginService.findByArtifactNameAndGroupId(plugin.getArtifactName(), plugin.getGroupId()))
                .thenReturn(List.of());
        when(pluginManager.startPlugin(plugin)).thenReturn(CompletableFuture.completedFuture(false));

        try (MockedStatic<PluginDownloadService> download = mockStatic(PluginDownloadService.class)) {
            download.when(() -> PluginDownloadService.downloadPlugin(request.repository(), plugin.toArtifact(), plugin))
                    .thenReturn(plugin);

            PluginUpdateResult result = updatePluginService.updatePlugins(request);

            assertTrue(result.updated().isEmpty());
            assertEquals(List.of(plugin), result.failed());
            verify(pluginInstallService).savePlugin(plugin);
        }
    }

    @Test
    void whenDownloadFailsThenReportFailureWithoutInstalling() {
        PluginDTO plugin = createPlugin();
        PluginsToUpdate request = new PluginsToUpdate(List.of(plugin), AppRepository.pluginRepo());
        plugin.setErrMsg("Download failed");

        try (MockedStatic<PluginDownloadService> download = mockStatic(PluginDownloadService.class)) {
            download.when(() -> PluginDownloadService.downloadPlugin(request.repository(), plugin.toArtifact(), plugin))
                    .thenReturn(plugin);

            PluginUpdateResult result = updatePluginService.updatePlugins(request);

            assertTrue(result.updated().isEmpty());
            assertEquals(List.of(plugin), result.failed());
            verify(pluginInstallService, never()).savePlugin(plugin);
        }
    }

    private PluginDTO createPlugin() {
        return new PluginDTO(new Plugin("com.example", "artifact-name", "", "1.0", "", ""));
    }
}
