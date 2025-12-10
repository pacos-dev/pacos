package org.pacos.core.component.plugin.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.pacos.config.repository.DependencyResolverException;
import org.pacos.config.repository.RepositoryClient;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.service.data.PluginsToUpdate;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.registry.service.RegistryService;
import org.pacos.core.system.service.SemanticVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is responsible for checking and returning a list of installed plugins that can be updated.
 */
@Component
public class PluginVersionChecker {

    private final PluginService pluginService;
    private final RegistryService registryService;

    @Autowired
    public PluginVersionChecker(PluginService pluginService, RegistryService registryService) {
        this.pluginService = pluginService;
        this.registryService = registryService;
    }

    /**
     * Calculates the list of plugins to update. Checks only installed and enabled plugins
     */
    @Transactional("coreTransactionManager")
    public PluginsToUpdate checkPluginsToUpdate() throws DependencyResolverException {
        List<PluginDTO> pluginDTOList = pluginService.findEnabledPlugin();
        if (pluginDTOList.isEmpty()) {
            registryService.saveRegistryImmediately(RegistryName.AVAILABLE_PLUGINS_COUNT_TO_UPDATE, 0);
            return new PluginsToUpdate(Collections.emptyList(), null);
        }
        AppRepository repository = AppRepository.pluginRepo();
        List<Plugin> list = RepositoryClient.loadPluginList(repository);
        List<PluginDTO> pluginToInstall = comparePlugins(list, pluginDTOList);
        registryService.saveRegistryImmediately(RegistryName.AVAILABLE_PLUGINS_COUNT_TO_UPDATE, pluginToInstall.size());
        return new PluginsToUpdate(pluginToInstall, repository);
    }

    private static List<PluginDTO> comparePlugins(List<Plugin> plugins, List<PluginDTO> pluginDTOList) {
        List<PluginDTO> pluginToInstall = new ArrayList<>();
        if (plugins == null) {
            return pluginToInstall;
        }
        plugins.forEach(plugin -> {
            Optional<PluginDTO> pluginOpt = pluginDTOList.stream().filter(pluginDTO ->
                    isTheSamePluginWithDifferentVersion(plugin, pluginDTO)).findFirst();
            if (pluginOpt.isPresent()) {
                SemanticVersion currentPlugin = new SemanticVersion(pluginOpt.get().getVersion());
                SemanticVersion newestPlugin = new SemanticVersion(plugin.version());
                if (newestPlugin.isNewestThan(currentPlugin)) {
                    pluginToInstall.add(new PluginDTO(plugin));
                }
            }
        });
        return pluginToInstall;
    }


    private static boolean isTheSamePluginWithDifferentVersion(Plugin pluginDTO, PluginDTO e) {
        return e.getArtifactName().equals(pluginDTO.artifactName()) &&
                e.getGroupId().equals(pluginDTO.groupId()) &&
                !e.getVersion().equals(pluginDTO.version());
    }
}
