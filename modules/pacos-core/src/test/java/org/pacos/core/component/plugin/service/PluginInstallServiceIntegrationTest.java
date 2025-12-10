package org.pacos.core.component.plugin.service;

import org.config.IntegrationTestContext;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.repository.PacosPluginRepository;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginInstallServiceIntegrationTest extends IntegrationTestContext {

    @Autowired
    private PluginInstallService pluginInstallService;
    @Autowired
    private PacosPluginRepository pluginRepository;

    @Test
    void whenSavePluginThenItSaved() {
        //given
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setGroupId("pl.pacos");
        pluginDTO.setArtifactName("test");
        pluginDTO.setVersion("1.0");
        pluginDTO.setName("Awesome test");
        //when
        pluginInstallService.savePlugin(pluginDTO);
        //then
        assertTrue(pluginRepository.findByGroupIdAndArtifactNameAndVersion(pluginDTO.getGroupId(), pluginDTO.getArtifactName(), pluginDTO.getVersion()).isPresent());
    }

    @Test
    void whenUpdatePluginThenRemoveOldPluginsIfExists() {
        //given
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setGroupId("pl.pacos");
        pluginDTO.setArtifactName("test");
        pluginDTO.setVersion("1.0");
        pluginDTO.setName("Awesome test");
        pluginInstallService.savePlugin(pluginDTO);

        //and when
        PluginDTO pluginDTO2 = new PluginDTO();
        pluginDTO2.setGroupId("pl.pacos");
        pluginDTO2.setArtifactName("test");
        pluginDTO2.setVersion("1.1");
        pluginDTO2.setName("Awesome test");
        pluginInstallService.savePlugin(pluginDTO2);
        //then
        assertEquals(1, pluginRepository.count());
        assertTrue(pluginRepository.findByGroupIdAndArtifactNameAndVersion(pluginDTO2.getGroupId(), pluginDTO2.getArtifactName(), pluginDTO2.getVersion()).isPresent());
    }

    @Test
    void whenUpdatePluginThenRemoveOldPlugins() {
        //given
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setGroupId("pl.pacos");
        pluginDTO.setArtifactName("test");
        pluginDTO.setVersion("1.0");
        pluginDTO.setName("Awesome test");
        pluginInstallService.savePlugin(pluginDTO);

        //and when
        PluginDTO pluginDTO2 = new PluginDTO();
        pluginDTO2.setGroupId("pl.pacos");
        pluginDTO2.setArtifactName("test");
        pluginDTO2.setVersion("1.1");
        pluginDTO2.setName("Awesome test");

        pluginInstallService.savePlugin(pluginDTO2);
        //then
        assertEquals(1, pluginRepository.count());
        assertTrue(pluginRepository.findByGroupIdAndArtifactNameAndVersion(pluginDTO.getGroupId(), pluginDTO2.getArtifactName(), pluginDTO2.getVersion()).isPresent());
    }


}
