package org.pacos.core.component.plugin.loader;

import org.pacos.config.repository.info.PluginInfo;

@FunctionalInterface
public interface PluginDetailsRefreshListener {

    void refreshed(PluginInfo pluginInfoDTO);
}
