package org.pacos.core.component.plugin.event;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.config.repository.info.Plugin;
import org.pacos.config.repository.info.PluginInfo;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.loader.PluginDetailsLoader;
import org.pacos.core.component.plugin.view.plugin.PluginDetails;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

class LoadPluginDetailEventTest {

    @Test
    void whenFireEventThenCallPluginDetailsLoader() {
        try (MockedStatic<PluginDetailsLoader> mock = mockStatic(PluginDetailsLoader.class)) {
            mock.when(() -> PluginDetailsLoader.loadPluginDetails(any(), any()))
                    .thenAnswer(inv -> null);
        }
        //when
        Plugin plugin = new Plugin("org", "test","test", "1.0","","");
        PluginDTO pluginDTO = new PluginDTO(plugin);
        PluginDetails pluginDetails = new PluginDetails();
        //when
        assertDoesNotThrow(() -> LoadPluginDetailEvent.fireEvent(pluginDTO, pluginDetails));
    }

    @Test
    void whenRefreshDetailsThenPassReceivedObject() {
        PluginDTO pluginDTO = mock(PluginDTO.class);
        PluginDetails pluginDetails = mock(PluginDetails.class);
        PluginInfo pluginInfo = mock(PluginInfo.class);
        //when
        LoadPluginDetailEvent.refreshDetails(pluginDTO, pluginDetails, pluginInfo);
        //then
        verify(pluginDTO).setPluginInfoDTO(pluginInfo);
        verify(pluginDetails).displayDetails(pluginDTO);
    }
}