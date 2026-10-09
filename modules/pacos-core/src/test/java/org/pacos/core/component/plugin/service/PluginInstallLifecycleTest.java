package org.pacos.core.component.plugin.service;

import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.domain.AppPlugin;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.repository.PacosPluginRepository;
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
        when(repository.findByArtifactNameAndGroupId(any(), any())).thenReturn(of());
    }

    @Test
    void initMarksPersistedPluginsAsFinishedAndSkipsTheirDownload() {
        PluginDTO installed = plugin();
        when(pluginService.findNotRemovedPlugin()).thenReturn(of(installed));
        pluginState.addPlugin(installed);

        installService.init();

        try (MockedStatic<PluginDownloadService> download = mockStatic(PluginDownloadService.class)) {
            installService.downloadAndInstallPluginFromRemote(installed, AppRepository.pluginRepo());
            download.verifyNoInteractions();
        }
        assertFalse(installService.isInstallationInProgress());
    }

    @Test
    void successfulDownloadPersistsPluginAndPublishesStartRequest() {
        PluginDTO requested = plugin();

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
        PluginDTO requested = plugin();
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
        PluginDTO requested = plugin();

        try (MockedStatic<PluginDownloadService> download = mockStatic(PluginDownloadService.class)) {
            download.when(() -> PluginDownloadService.downloadPlugin(
                    any(AppRepository.class), any(), any(PluginDTO.class))).thenReturn(requested);
            installService.downloadAndInstallPluginFromRemote(requested, AppRepository.pluginRepo());
            installService.downloadAndInstallPluginFromRemote(requested, AppRepository.pluginRepo());

            download.verify(() -> PluginDownloadService.downloadPlugin(
                    any(AppRepository.class), any(), any(PluginDTO.class)));
        }
    }

    @Test
    void runtimeFailureDuringPersistenceClearsDownloadStatusAndPropagates() {
        PluginDTO requested = plugin();
        doThrow(new IllegalStateException("database unavailable"))
                .when(repository).save(any(AppPlugin.class));

        try (MockedStatic<PluginDownloadService> download = mockStatic(PluginDownloadService.class)) {
            download.when(() -> PluginDownloadService.downloadPlugin(
                    any(AppRepository.class), any(), any(PluginDTO.class))).thenReturn(requested);
            AppRepository appRepository = AppRepository.pluginRepo();
            assertThrows(IllegalStateException.class,
                    () -> installService.downloadAndInstallPluginFromRemote(requested, appRepository));

            assertFalse(installService.isInstallationInProgress());
        }
    }

    @Test
    void uploadedPluginFileIsDelegatedToStorageService() throws Exception {
        UploadedPluginInfo info = new UploadedPluginInfo(plugin(), new byte[] { 1, 2, 3 }, "plugin.jar");

        installService.storePluginFile(info);

        verify(fileStorageService).storePluginFile(info);
    }

    private static PluginDTO plugin() {
        return new PluginDTO(new Plugin("com.example", "coverage-plugin", "", "1.0", "", ""));
    }
}
