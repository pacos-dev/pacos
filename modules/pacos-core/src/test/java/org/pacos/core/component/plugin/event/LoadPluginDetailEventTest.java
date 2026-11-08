package org.pacos.core.component.plugin.event;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.info.Plugin;
import org.pacos.config.repository.info.PluginInfo;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.loader.PluginDetailsLoader;
import org.pacos.core.component.plugin.view.plugin.PluginDetails;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

class LoadPluginDetailEventTest {

    @Test
    void whenFireEventThenCallPluginDetailsLoader() {
        Plugin plugin = new Plugin("org", "test", "test", "1.0", "", "");
        PluginDTO pluginDTO = new PluginDTO(plugin);
        PluginDetails pluginDetails = new PluginDetails();

        try (MockedStatic<PluginDetailsLoader> loader = mockStatic(PluginDetailsLoader.class)) {
            loader.when(() -> PluginDetailsLoader.loadPluginDetails(any(), any(), any(), any()))
                    .thenAnswer(inv -> null);

            assertDoesNotThrow(() -> LoadPluginDetailEvent.fireEvent(pluginDTO, pluginDetails));

            loader.verify(() -> PluginDetailsLoader.loadPluginDetails(any(), any(), any(), any()));
        }
    }

    @Test
    void whenFireEventWithRepositoryThenPassRepositoryToLoader() {
        Plugin plugin = new Plugin("org", "test", "test", "1.0", "", "");
        PluginDTO pluginDTO = new PluginDTO(plugin);
        PluginDetails pluginDetails = new PluginDetails();
        AppRepository repository = new AppRepository("partner", "https://repo.example.com/maven");

        try (MockedStatic<PluginDetailsLoader> loader = mockStatic(PluginDetailsLoader.class)) {
            loader.when(() -> PluginDetailsLoader.loadPluginDetails(
                    any(AppArtifact.class), eq(repository), eq(pluginDetails), any())).thenAnswer(invocation -> null);

            LoadPluginDetailEvent.fireEvent(pluginDTO, repository, pluginDetails);

            loader.verify(() -> PluginDetailsLoader.loadPluginDetails(
                    any(AppArtifact.class), eq(repository), eq(pluginDetails), any()));
        }
    }

    @Test
    void whenRefreshDetailsThenPassReceivedObject() {
        PluginDTO pluginDTO = mock(PluginDTO.class);
        PluginDetails pluginDetails = mock(PluginDetails.class);
        PluginInfo pluginInfo = mock(PluginInfo.class);

        LoadPluginDetailEvent.refreshDetails(pluginDTO, pluginDetails, pluginInfo);

        verify(pluginDTO).setPluginInfoDTO(pluginInfo);
        verify(pluginDetails).displayDetails(pluginDTO);
    }
}
