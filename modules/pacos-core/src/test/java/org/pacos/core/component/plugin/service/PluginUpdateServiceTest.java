package org.pacos.core.component.plugin.service;

import java.util.List;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.manager.SwaggerUIConfigReload;
import org.pacos.core.component.plugin.service.data.PluginUpdateResult;
import org.pacos.core.component.plugin.service.data.PluginsToUpdate;
import org.pacos.core.component.session.service.ServiceListener;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PluginUpdateServiceTest {

    private PluginUpdateService updatePluginService;
    private PluginInstallService pluginInstallService;
    private PluginService pluginService;
    private PluginManager pluginManager;

    @BeforeEach
    void setUp() {
        pluginInstallService = Mockito.mock(PluginInstallService.class);
        pluginService = mock(PluginService.class);
        pluginManager = new PluginManager(pluginService,
                Mockito.mock(SwaggerUIConfigReload.class),
                Mockito.mock(ApplicationContext.class));
        pluginManager.initializePluginsOnApplicationReadyEvent();
        updatePluginService = new PluginUpdateService(pluginInstallService, pluginManager, pluginService);
    }

    @Test
    void whenNoPluginsToUpdateThenReturnEmptyList() {
        PluginsToUpdate pluginsToUpdate = new PluginsToUpdate(List.of(), AppRepository.pluginRepo());
        //when
        PluginUpdateResult result = updatePluginService.updatePlugins(pluginsToUpdate);
        //then
        assertTrue(result.updated().isEmpty());
        assertTrue(result.failed().isEmpty());
    }

    @Test
    void whenPluginUpdatedThenReturnPluginUpdateResult() {
        VaadinMock.mockSystem();
        PluginDTO pluginDTO = new PluginDTO(new Plugin("com.example", "artifact-name", "", "1.0", "", ""));
        PluginsToUpdate pluginsToUpdate = new PluginsToUpdate(List.of(pluginDTO), AppRepository.pluginRepo());
        pluginManager.addPlugin(pluginDTO);
        when(pluginService.findByArtifactNameAndGroupId(pluginDTO.getArtifactName(), pluginDTO.getGroupId())).thenReturn(List.of(pluginDTO));

        //when
        try (MockedStatic<PluginDownloadService> downloadServiceMock = mockStatic(PluginDownloadService.class);
             MockedStatic<ServiceListener> serviceListenerMock = mockStatic(ServiceListener.class)) {
            serviceListenerMock.when(() -> ServiceListener.notifyAll(any(), any())).thenAnswer(inv -> null);
            downloadServiceMock.when(() ->
                    PluginDownloadService.downloadPlugin(pluginsToUpdate.repository(), pluginDTO.toArtifact(), pluginDTO)).thenReturn(pluginDTO);
            //when
            PluginUpdateResult result = updatePluginService.updatePlugins(pluginsToUpdate);
            //then
            verify(pluginInstallService).savePlugin(pluginDTO);

            assertTrue(result.updated().contains(pluginDTO));
            assertTrue(result.failed().isEmpty());
        }
    }

    @Test
    void whenPluginFailedThenReturnPluginUpdateResult() {
        PluginDTO pluginDTO = new PluginDTO(new Plugin("com.example", "artifact-name", "", "1.0", "", ""));
        PluginsToUpdate pluginsToUpdate = new PluginsToUpdate(List.of(pluginDTO), AppRepository.pluginRepo());
        //when
        try (MockedStatic<PluginDownloadService> downloadServiceMock = mockStatic(PluginDownloadService.class)) {
            pluginDTO.setErrMsg("Can't download plugin");
            downloadServiceMock.when(() ->
                    PluginDownloadService.downloadPlugin(pluginsToUpdate.repository(), pluginDTO.toArtifact(), pluginDTO)).thenReturn(pluginDTO);
            //when
            PluginUpdateResult result = updatePluginService.updatePlugins(pluginsToUpdate);
            //then
            verifyNoInteractions(pluginInstallService);

            assertTrue(result.updated().isEmpty());
            assertTrue(result.failed().contains(pluginDTO));
        }
    }

}