package org.pacos.core.component.plugin.service;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pacos.config.repository.RepositoryClient;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.service.data.PluginsToUpdate;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.registry.service.RegistryService;

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

    @BeforeEach
    void setUp() {
        this.pluginService = Mockito.mock(PluginService.class);
        this.registryService = Mockito.mock(RegistryService.class);
        this.pluginVersionChecker = new PluginVersionChecker(pluginService, registryService);
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
    void whenPluginToUpdateThenUpdateRegistryValueAndReturnPluginToUpdate() {
        //given
        PluginDTO installed = new PluginDTO(new Plugin("org.pacos", "test", "", "1.0", "", ""));
        Plugin inMarket = new Plugin("org.pacos", "test", "", "1.1", "", "");
        when(pluginService.findEnabledPlugin()).thenReturn(List.of(installed));

        try (MockedStatic<RepositoryClient> repositoryClientMock = mockStatic(RepositoryClient.class)) {
            repositoryClientMock.when(() -> RepositoryClient.loadPluginList(any())).thenReturn(List.of(inMarket));
            //when
            PluginsToUpdate pluginsToUpdate = pluginVersionChecker.checkPluginsToUpdate();
            assertEquals(1, pluginsToUpdate.plugins().size());
        }
        //then
        verify(registryService).saveRegistryImmediately(RegistryName.AVAILABLE_PLUGINS_COUNT_TO_UPDATE, 1);
    }

    @Test
    void whenMajorVersionChangedThenReturnToUpdate() {
        //given
        PluginDTO installed = new PluginDTO(new Plugin("org.pacos", "test", "", "1.0", "", ""));
        Plugin inMarket = new Plugin("org.pacos", "test", "", "2.0", "", "");
        when(pluginService.findEnabledPlugin()).thenReturn(List.of(installed));

        try (MockedStatic<RepositoryClient> repositoryClientMock = mockStatic(RepositoryClient.class)) {
            repositoryClientMock.when(() -> RepositoryClient.loadPluginList(any())).thenReturn(List.of(inMarket));
            //when
            PluginsToUpdate pluginsToUpdate = pluginVersionChecker.checkPluginsToUpdate();
            assertEquals(1, pluginsToUpdate.plugins().size());
        }
        //then
        verify(registryService).saveRegistryImmediately(RegistryName.AVAILABLE_PLUGINS_COUNT_TO_UPDATE, 1);
    }

    @Test
    void whenVersionIsTheSameThenDoNotReturnToUpdate() {
        //given
        PluginDTO installed = new PluginDTO(new Plugin("org.pacos", "test", "", "1.0", "", ""));
        Plugin inMarket = new Plugin("org.pacos", "test", "", "1.0", "", "");
        when(pluginService.findEnabledPlugin()).thenReturn(List.of(installed));

        try (MockedStatic<RepositoryClient> repositoryClientMock = mockStatic(RepositoryClient.class)) {
            repositoryClientMock.when(() -> RepositoryClient.loadPluginList(any())).thenReturn(List.of(inMarket));
            //when
            PluginsToUpdate pluginsToUpdate = pluginVersionChecker.checkPluginsToUpdate();
            assertEquals(0, pluginsToUpdate.plugins().size());
        }
        //then
        verify(registryService).saveRegistryImmediately(RegistryName.AVAILABLE_PLUGINS_COUNT_TO_UPDATE, 0);
    }

}