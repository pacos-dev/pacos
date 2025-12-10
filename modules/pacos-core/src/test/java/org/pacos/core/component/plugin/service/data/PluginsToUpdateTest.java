package org.pacos.core.component.plugin.service.data;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.dto.PluginDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PluginsToUpdateTest {

    @Test
    void whenCreatePluginsToUpdateThenFieldsShouldBeSet() {
        Plugin plugin = new Plugin("org.pacos", "test", "", "1.0", "", "");
        PluginDTO pluginDTO = new PluginDTO(plugin);
        AppRepository repository = AppRepository.pluginRepo();

        List<PluginDTO> plugins = List.of(pluginDTO);

        PluginsToUpdate pluginsToUpdate = new PluginsToUpdate(plugins, repository);

        assertEquals(plugins, pluginsToUpdate.plugins());
        assertEquals(repository, pluginsToUpdate.repository());
    }

    @Test
    void whenCompareEqualPluginsToUpdateThenShouldBeEqual() {
        Plugin plugin = new Plugin("org.pacos", "test", "", "1.0", "", "");
        PluginDTO pluginDTO = new PluginDTO(plugin);
        AppRepository repository = new AppRepository("repo", "repo");

        List<PluginDTO> plugins = List.of(pluginDTO);

        PluginsToUpdate pluginsToUpdate1 = new PluginsToUpdate(plugins, repository);
        PluginsToUpdate pluginsToUpdate2 = new PluginsToUpdate(plugins, repository);

        assertEquals(pluginsToUpdate1, pluginsToUpdate2);
        assertEquals(pluginsToUpdate1.hashCode(), pluginsToUpdate2.hashCode());
    }

    @Test
    void whenCompareDifferentPluginsToUpdateThenShouldNotBeEqual() {
        Plugin plugin1 = new Plugin("org.pacos", "test", "", "1.0", "", "");
        PluginDTO pluginDTO1 = new PluginDTO(plugin1);
        Plugin plugin2 = new Plugin("org.pacos", "test", "", "2.0", "", "");
        PluginDTO pluginDTO2 = new PluginDTO(plugin2);


        AppRepository repository1 = new AppRepository("repo", "repo");
        AppRepository repository2 = new AppRepository("repo2", "repo");

        PluginsToUpdate pluginsToUpdate1 = new PluginsToUpdate(List.of(pluginDTO1), repository1);
        PluginsToUpdate pluginsToUpdate2 = new PluginsToUpdate(List.of(pluginDTO2), repository2);

        assertNotEquals(pluginsToUpdate1, pluginsToUpdate2);
    }
}
