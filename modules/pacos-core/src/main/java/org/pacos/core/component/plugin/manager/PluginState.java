package org.pacos.core.component.plugin.manager;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;
import org.springframework.stereotype.Component;

@Component
public class PluginState {
    private final Map<PluginDTO, PluginStatusEnum> pluginStateMap = new ConcurrentHashMap<>();

    public PluginStatusEnum removePlugin(PluginDTO pluginDTO) {
        return pluginStateMap.remove(pluginDTO);
    }

    public boolean canRun(PluginDTO plugin) {
        PluginStatusEnum state = pluginStateMap.get(plugin);
        return state != null && state.canRun();
    }

    public boolean canStop(PluginDTO plugin) {
        PluginStatusEnum state = pluginStateMap.get(plugin);
        return state != null && state.canStop();
    }

    public PluginStatusEnum getState(PluginDTO plugin) {
        return pluginStateMap.get(plugin);
    }

    public Set<PluginDTO> getPlugins() {
        return new HashSet<>(pluginStateMap.keySet());
    }

    public void addPlugin(PluginDTO plugin) {
        pluginStateMap.putIfAbsent(plugin, PluginStatusEnum.OFF);
    }

    public void setState(PluginDTO plugin, PluginStatusEnum pluginStateEnum) {
        pluginStateMap.put(plugin, pluginStateEnum);
    }

    public Optional<PluginDTO> isInstallationInProgress() {
        return pluginStateMap.entrySet().stream()
                .filter(e -> e.getValue().isInitialized())
                .map(Map.Entry::getKey)
                .findFirst();
    }
}
