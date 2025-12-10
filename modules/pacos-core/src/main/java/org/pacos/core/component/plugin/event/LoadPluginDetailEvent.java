package org.pacos.core.component.plugin.event;

import org.pacos.config.repository.info.PluginInfo;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.loader.PluginDetailsLoader;
import org.pacos.core.component.plugin.view.plugin.PluginDetails;

public final class LoadPluginDetailEvent {

    private LoadPluginDetailEvent() {
    }

    public static void fireEvent(PluginDTO plugin, PluginDetails pluginDetails) {
        PluginDetailsLoader.loadPluginDetails(
                plugin.toArtifact(), details -> refreshDetails(plugin, pluginDetails, details));
    }

    static void refreshDetails(PluginDTO plugin, PluginDetails pluginDetails, PluginInfo details) {
        plugin.setPluginInfoDTO(details);
        pluginDetails.displayDetails(plugin);
    }
}
