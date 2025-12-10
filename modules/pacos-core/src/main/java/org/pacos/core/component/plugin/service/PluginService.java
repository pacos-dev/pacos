package org.pacos.core.component.plugin.service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.pacos.base.exception.PacosException;
import org.pacos.config.property.WorkingDir;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.core.component.plugin.domain.AppPlugin;
import org.pacos.core.component.plugin.dto.PacosPluginDTOMapper;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.repository.PacosPluginRepository;
import org.pacos.core.system.service.SemanticVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PluginService {

    private static final Logger LOG = LoggerFactory.getLogger(PluginService.class);
    private final PacosPluginRepository pluginRepository;


    @Autowired
    public PluginService(PacosPluginRepository pluginRepository) {
        this.pluginRepository = pluginRepository;
    }


    public List<PluginDTO> findAll() {
        return pluginRepository.findAll().stream().map(PacosPluginDTOMapper::map)
                .toList();
    }

    public void disablePlugin(PluginDTO pluginDTO) {
        LOG.info("Disable plugin {}", pluginDTO);
        AppPlugin plugin = loadPluginWithoutVersion(pluginDTO, false);
        pluginDTO.setDisabled(true);
        plugin.setDisabled(true);
        pluginRepository.save(plugin);
    }

    public void enablePlugin(PluginDTO pluginDTO) {
        LOG.info("Enable plugin {}", pluginDTO);
        AppPlugin plugin = loadPluginWithoutVersion(pluginDTO, true);
        pluginDTO.setDisabled(false);
        plugin.setDisabled(false);
        pluginRepository.save(plugin);
    }

    @Transactional("coreTransactionManager")
    public void removePlugin(PluginDTO pluginDTO) {
        LOG.info("Remove plugin {}", pluginDTO);
        List<AppPlugin> plugins = pluginRepository.findByArtifactNameAndGroupId(pluginDTO.getArtifactName(), pluginDTO.getGroupId());
        plugins.forEach(plugin -> {
            AppArtifact artifact = new AppArtifact(pluginDTO.getGroupId(), pluginDTO.getArtifactName(), pluginDTO.getVersion());
            try {
                Files.delete(WorkingDir.getLibPath().resolve(artifact.getJarPath()));
                LOG.info("Plugin file has been deleted {}", artifact.getJarPath());
            } catch (IOException e) {
                LOG.warn("Failed to delete lib file {}", artifact.getJarPath());
            }
        });
        pluginRepository.deleteAll(plugins);
    }

    @Transactional("coreTransactionManager")
    public void removePlugin(AppPlugin plugin) {
        LOG.info("Remove plugin {}", plugin);
        pluginRepository.delete(plugin);
    }

    private AppPlugin loadPluginWithoutVersion(PluginDTO pluginDTO, boolean disabled) {
        return pluginRepository.findByGroupIdAndArtifactNameAndDisabled(pluginDTO.getGroupId(), pluginDTO.getArtifactName(), disabled)
                .orElseThrow(() -> new PacosException("Plugin doesn't exists " + pluginDTO));
    }

    @Transactional("coreTransactionManager")
    public void disablePluginWithDifferentMajorVersion(SemanticVersion newVersion) {
        pluginRepository.disableAllPluginsWhereVersionIsNotLike(newVersion.getMajorVersion() + ".%");
    }

    public List<PluginDTO> findEnabledPlugin() {
        return pluginRepository.findAllByDisabled(false).stream().map(PacosPluginDTOMapper::map).toList();
    }

    public List<PluginDTO> findNotRemovedPlugin() {
        return pluginRepository.findAllByRemoved(false).stream().map(PacosPluginDTOMapper::map).toList();
    }

    public List<PluginDTO> findByArtifactNameAndGroupId(String artifactName, String groupId) {
        return pluginRepository.findByArtifactNameAndGroupId(artifactName, groupId)
                .stream().map(PacosPluginDTOMapper::map).toList();
    }
}
