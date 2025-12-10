package org.pacos.core.component.plugin.view.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.pacos.config.repository.data.AppRepository;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.system.service.SemanticVersion;

public record PluginHandler(List<PluginDTO> installed, List<PluginDTO> marketPlace,
                            AppRepository repository) {

    public List<PluginDTO> getPluginToDisplay() {
        return new ArrayList<>(marketPlace);
    }

    public boolean isInstalled(PluginDTO pluginDTO) {
        return installed.stream().anyMatch(p -> p.equalsWithoutVersion(pluginDTO));
    }

    public boolean isToUpdate(PluginDTO pluginDTO) {
        Optional<PluginDTO> installedPlugin = installed.stream().filter(p -> p.equalsWithoutVersion(pluginDTO)).findAny();
        if (installedPlugin.isPresent()) {
            SemanticVersion marketPluginVersion = new SemanticVersion(pluginDTO.getVersion());
            SemanticVersion installedPluginVersion = new SemanticVersion(installedPlugin.get().getVersion());
            return marketPluginVersion.isNewestThan(installedPluginVersion);
        }
        return false;
    }
}
