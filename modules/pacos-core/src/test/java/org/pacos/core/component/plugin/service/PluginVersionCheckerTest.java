package org.pacos.core.component.plugin.service;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pacos.config.repository.RepositoryClient;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.service.data.PluginsToUpdate;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.registry.service.RegistryService;
import org.pacos.core.component.repository.service.PluginRepositoryService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PluginVersionCheckerTest {

    private PluginVersionChecker pluginVersionChecker;

    private PluginService pluginService;
    private RegistryService registryService;
    private PluginRepositoryService pluginRepositoryService;

    @BeforeEach
    void setUp() {
        this.pluginService = Mockito.mock(PluginService.class);
        this.registryService = Mockito.mock(RegistryService.class);
        this.pluginRepositoryService = Mockito.mock(PluginRepositoryService.class);
        Mockito.when(pluginRepositoryService.getSelectedAppRepository()).thenReturn(AppRepository.pluginRepo());
        this.pluginVersionChecker = new PluginVersionChecker(pluginService, registryService, pluginRepositoryService);
    }

    @Test
    void whenCheckIfPluginToUpdateButAnyPluginInstalledThenReturnEmptyResult() {
        //given
        when(pluginService.findEnabledPlugin()).thenReturn(Collections.emptyList());
        //when
        PluginsToUpdate pluginsToUpdate = pluginVersionChecker.checkPluginsToUpdate();
        //then
        assertTrue(pluginsToUpdate.plugins().isEmpty());
    }

    @Test
    void whenNoPluginToUpdateThenUpdateRegistryValue() {
        //given
        when(pluginService.findEnabledPlugin()).thenReturn(Collections.emptyList());
        //when
        pluginVersionChecker.checkPluginsToUpdate();
        //then
        verify(registryService).saveRegistryImmediately(RegistryName.AVAILABLE_PLUGINS_COUNT_TO_UPDATE, 0);
    }

    @Test
    void whenCheckPluginsToUpdateThenUseSelectedRepository() {
        PluginDTO installed = new PluginDTO(new Plugin("org.pacos", "test", "", "1.0", "", ""));
        AppRepository selectedRepository = new AppRepository("partner", "https://repo.example.com/maven");
        when(pluginService.findEnabledPlugin()).thenReturn(List.of(installed));
        when(pluginRepositoryService.getSelectedAppRepository()).thenReturn(selectedRepository);

        try (MockedStatic<RepositoryClient> repositoryClient = mockStatic(RepositoryClient.class)) {
            repositoryClient.when(() -> RepositoryClient.loadPluginList(selectedRepository)).thenReturn(List.of());

            PluginsToUpdate result = pluginVersionChecker.checkPluginsToUpdate();

            assertTrue(result.plugins().isEmpty());
            assertEquals(selectedRepository, result.repository());
            repositoryClient.verify(() -> RepositoryClient.loadPluginList(selectedRepository));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "1.1, 1",
            "2.0, 1",
            "1.0, 0"
    })
    void whenMarketVersionComparedThenExpectedUpdateCountIsReturned(String marketVersion, int expectedUpdateCount) {
        PluginDTO installed = new PluginDTO(new Plugin("org.pacos", "test", "", "1.0", "", ""));
        Plugin inMarket = new Plugin("org.pacos", "test", "", marketVersion, "", "");
        when(pluginService.findEnabledPlugin()).thenReturn(List.of(installed));

        try (MockedStatic<RepositoryClient> repositoryClientMock = mockStatic(RepositoryClient.class)) {
            repositoryClientMock.when(() -> RepositoryClient.loadPluginList(any())).thenReturn(List.of(inMarket));
            PluginsToUpdate pluginsToUpdate = pluginVersionChecker.checkPluginsToUpdate();
            assertEquals(expectedUpdateCount, pluginsToUpdate.plugins().size());
        }

        verify(registryService).saveRegistryImmediately(
                RegistryName.AVAILABLE_PLUGINS_COUNT_TO_UPDATE, expectedUpdateCount);
    }
}