package org.pacos.core.component.plugin.service;

import java.util.List;
import java.util.Optional;

import org.config.IntegrationTestContext;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.plugin.domain.AppPlugin;
import org.pacos.core.component.plugin.dto.PacosPluginDTOMapper;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.repository.PacosPluginRepository;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginServiceIntegrationTest extends IntegrationTestContext {

    @Autowired
    private PluginService service;
    @Autowired
    private PacosPluginRepository pacosPluginRepository;

    @Test
    void whenRemovePluginThenPluginIsRemoved() {
        //given
        AppPlugin plugin = new AppPlugin("test", "test", "test");
        pacosPluginRepository.save(plugin);

        PluginDTO dto = PacosPluginDTOMapper.map(plugin);
        //when
        service.removePlugin(dto);
        //then
        assertEquals(0, pacosPluginRepository.count());
    }

    @Test
    void whenEnablePluginThenUpdateEntry() {
        //given
        AppPlugin plugin = new AppPlugin("test", "test", "test");
        plugin.setDisabled(true);
        pacosPluginRepository.save(plugin);
        PluginDTO dto = PacosPluginDTOMapper.map(plugin);
        //when
        service.enablePlugin(dto);
        //then
        assertFalse(dto.isDisabled());
    }

    @Test
    void whenDisablePluginThenUpdateEntry() {
        //given
        AppPlugin plugin = new AppPlugin("test", "test", "test");
        pacosPluginRepository.save(plugin);
        PluginDTO dto = PacosPluginDTOMapper.map(plugin);
        //when
        service.disablePlugin(dto);
        //then
        assertTrue(dto.isDisabled());
    }

    @Test
    void whenFindAllThenReturnAllPlugins() {
        //given
        AppPlugin plugin = new AppPlugin("test", "test", "1.0");
        AppPlugin plugin2 = new AppPlugin("org", "pacos", "1.1");
        pacosPluginRepository.save(plugin);
        pacosPluginRepository.save(plugin2);
        //when
        List<PluginDTO> pluginDTOList = service.findAll();
        //then
        assertEquals(2, pluginDTOList.size());
    }

    @Test
    void whenDisableIncompatiblePluginThenPluginIsDisabled() {
        AppPlugin plugin = new AppPlugin("test", "test", "1.0");
        pacosPluginRepository.save(plugin);
        //when
        pacosPluginRepository.disableAllPluginsWhereVersionIsNotLike("2.%");
        //then
        Optional<AppPlugin> byId = pacosPluginRepository.findById(plugin.getId());
        assertTrue(byId.isPresent());
        assertTrue(byId.get().isDisabled());
    }

    @Test
    void whenDisableIncompatiblePluginThenPluginIsEnabled() {
        AppPlugin plugin = new AppPlugin("test", "test", "1.0");
        pacosPluginRepository.save(plugin);
        //when
        pacosPluginRepository.disableAllPluginsWhereVersionIsNotLike("1.%");
        //then
        Optional<AppPlugin> byId = pacosPluginRepository.findById(plugin.getId());
        assertTrue(byId.isPresent());
        assertFalse(byId.get().isDisabled());
    }

    @Test
    void whenFindByDisabledThenReturnAllEnabledPlugins() {
        AppPlugin plugin = new AppPlugin("test", "test", "1.0");
        plugin.setDisabled(true);
        pacosPluginRepository.save(plugin);
        plugin = new AppPlugin("test", "test2", "1.1");
        plugin.setDisabled(false);
        pacosPluginRepository.save(plugin);
        plugin = new AppPlugin("test", "test3", "1.1");
        plugin.setDisabled(false);
        pacosPluginRepository.save(plugin);
        //when
        List<AppPlugin> enabledPlugins = pacosPluginRepository.findAllByDisabled(false);
        //then
        assertEquals(2, enabledPlugins.size());
        assertTrue(enabledPlugins.stream().anyMatch(e -> e.getArtifactName().equals("test2")));
        assertTrue(enabledPlugins.stream().anyMatch(e -> e.getArtifactName().equals("test3")));
    }


}