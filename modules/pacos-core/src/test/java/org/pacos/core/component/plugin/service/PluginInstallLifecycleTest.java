package org.pacos.core.component.plugin.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.domain.AppPlugin;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.repository.PacosPluginRepository;
import org.pacos.core.component.plugin.view.plugin.DownloadPluginStatus;
import org.springframework.context.ApplicationEventPublisher;

class PluginInstallLifecycleTest {

    private PacosPluginRepository repository;
    private PluginService pluginService;
    private PluginState pluginState;
    private PluginFileStorageService fileStorageService;
    private ApplicationEventPublisher eventPublisher;
    private PluginInstallService installService;

    @BeforeEach
    void setUp() {
        repository = mock(PacosPluginRepository.class);
        pluginService = mock(PluginService.class);
        pluginState = new PluginState();
        fileStorageService = mock(PluginFileStorageService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        installService = new PluginInstallService(
                repository, pluginService, pluginState, fileStorageService, eventPublisher);
        when(repository.findByArtifactNameAndGroupId(any(), any())).thenReturn(List.of());
    }

    @Test
    void initMarksPersistedPluginsAsFinished() {
        PluginDTO installed = plugin("1.0");
        when(pluginService.findNotRemovedPlugin()).thenReturn(List.of(installed));

        installService.init();
        installService.downloadAndInstallPluginFromRemote(installed, AppRepository.pluginRepo());

        assertFalse(installService.isInstallationInProgress());
    }

    @Test
    void successfulDownloadPersistsPluginAndPublishesStartRequest() {
        PluginDTO requested = plugin("1.0");
        when(pluginService.findNotRemovedPlugin()).thenReturn(List.of());

        try (MockedStatic<PluginDownloadService> download = mockStatic(PluginDownloadService.class)) {
            download.when(() -> PluginDownloadService.downloadPlugin(
                    any(AppRepository.class), any(), any(PluginDTO.class))).thenReturn(requested);

            installService.downloadAndInstallPluginFromRemote(requested, AppRepository.pluginRepo());

            verify(repository).save(any(AppPlugin.class));
            verify(eventPublisher).publishEvent(new PluginStartRequestedEvent(requested));
            assertTrue(pluginState.getPlugins().contains(requested));
            assertFalse(installService.isInstallationInProgress());
        }
    }

    @Test
    void failedDownloadClearsInProgressStatusAndDoesNotPersistPlugin() {
        PluginDTO requested = plugin("1.0");
        requested.setErrMsg("repository unavailable");

        try (MockedStatic<PluginDownloadService> download = mockStatic(PluginDownloadService.class)) {
            download.when(() -> PluginDownloadService.downloadPlugin(
                    any(AppRepository.class), any(), any(PluginDTO.class))).thenReturn(requested);

            installService.downloadAndInstallPluginFromRemote(requested, AppRepository.pluginRepo());

            verify(repository, org.mockito.Mockito.never()).save(any(AppPlugin.class));
            assertFalse(installService.isInstallationInProgress());
        }
    }

    @Test
    void duplicateInstallOfAlreadyInstalledPluginDoesNotDownloadAgain() {
        PluginDTO requested = plugin("1.0");
        pluginState.addPlugin(requested);

        try (MockedStatic<PluginDownloadService> download = mockStatic(PluginDownloadService.class)) {
            // Simulate a completed entry by running a successful installation once.
            download.when(() -> PluginDownloadService.downloadPlugin(
                    any(AppRepository.class), any(), any(PluginDTO.class))).thenReturn(requested);
            installService.downloadAndInstallPluginFromRemote(requested, AppRepository.pluginRepo());
            installService.downloadAndInstallPluginFromRemote(requested, AppRepository.pluginRepo());

            download.verify(org.mockito.Mockito.times(1), () -> PluginDownloadService.downloadPlugin(
                    any(AppRepository.class), any(), any(PluginDTO.class)));
        }
    }

    @Test
    void runtimeFailureDuringPersistenceClearsDownloadStatusAndPropagates() {
        PluginDTO requested = plugin("1.0");
        doThrow(new IllegalStateException("database unavailable"))
                .when(repository).save(any(AppPlugin.class));

        try (MockedStatic<PluginDownloadService> download = mockStatic(PluginDownloadService.class)) {
            download.when(() -> PluginDownloadService.downloadPlugin(
                    any(AppRepository.class), any(), any(PluginDTO.class))).thenReturn(requested);

            org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                    () -> installService.downloadAndInstallPluginFromRemote(requested, AppRepository.pluginRepo()));

            assertFalse(installService.isInstallationInProgress());
        }
    }

    @Test
    void uploadedPluginFileIsDelegatedToStorageService() throws Exception {
        UploadedPluginInfo info = new UploadedPluginInfo(plugin("1.0"), new byte[] {1, 2, 3}, "plugin.jar");

        installService.storePluginFile(info);

        verify(fileStorageService).storePluginFile(info);
    }

    private static PluginDTO plugin(String version) {
        return new PluginDTO(new Plugin("com.example", "coverage-plugin", "", version, "", ""));
    }
}
