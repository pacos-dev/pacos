package org.pacos.core.component.plugin.loader;

import java.util.List;

import org.pacos.config.repository.info.Plugin;

@FunctionalInterface
public interface PluginRefreshListener {

    void listLoaded(List<Plugin> pluginList);
}
