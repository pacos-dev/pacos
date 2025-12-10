package org.pacos.core.component.plugin.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;

/**
 * This clas holds all status of the available plugins
 */
public final class PluginState {
    private static final Map<PluginDTO, PluginStatusEnum> pluginStateMap = new HashMap<>();

    private PluginState() {
    }

    static PluginStatusEnum removePlugin(PluginDTO pluginDTO) {
        return pluginStateMap.remove(pluginDTO);
    }

    public static boolean canRun(PluginDTO plugin) {
        return pluginStateMap.get(plugin).canRun();
    }

    public static boolean canStop(PluginDTO plugin) {
        return pluginStateMap.get(plugin).canStop();
    }

    public static PluginStatusEnum getState(PluginDTO plugin) {
        return pluginStateMap.get(plugin);
    }

    public static Set<PluginDTO> getPlugins() {
        return new HashSet<>(pluginStateMap.keySet());
    }

    static void addPlugin(PluginDTO plugin) {
        pluginStateMap.put(plugin, PluginStatusEnum.OFF);
    }

    static void setState(PluginDTO plugin, PluginStatusEnum pluginStateEnum) {
        pluginStateMap.put(plugin, pluginStateEnum);
    }

    public static Optional<PluginDTO> isInstallationInProgress() {
        return pluginStateMap.entrySet().stream()
                .filter(e -> e.getValue().isInitialized())
                .map(Map.Entry::getKey)
                .findFirst();
    }
}
