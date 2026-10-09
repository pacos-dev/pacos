package org.pacos.core.component.plugin.manager;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;
import org.springframework.stereotype.Component;

@Component
public class PluginState {
    private final ConcurrentMap<PluginKey, PluginStatusEnum> states = new ConcurrentHashMap<>();
    private final ConcurrentMap<PluginKey, PluginDTO> plugins = new ConcurrentHashMap<>();

    public PluginStatusEnum removePlugin(PluginDTO plugin) {
        PluginKey key = PluginKey.from(plugin);
        plugins.remove(key);
        return states.remove(key);
    }

    public boolean canRun(PluginDTO plugin) {
        PluginStatusEnum state = getState(plugin);
        return state != null && state.canRun();
    }

    public boolean canStop(PluginDTO plugin) {
        PluginStatusEnum state = getState(plugin);
        return state != null && state.canStop();
    }

    public PluginStatusEnum getState(PluginDTO plugin) {
        return states.get(PluginKey.from(plugin));
    }

    public Set<PluginDTO> getPlugins() {
        return new HashSet<>(plugins.values());
    }

    public void addPlugin(PluginDTO plugin) {
        PluginKey key = PluginKey.from(plugin);
        plugins.putIfAbsent(key, plugin);
        states.putIfAbsent(key, PluginStatusEnum.OFF);
    }

    public void setState(PluginDTO plugin, PluginStatusEnum state) {
        PluginKey key = PluginKey.from(plugin);
        plugins.putIfAbsent(key, plugin);
        states.put(key, state);
    }

    public Optional<PluginDTO> isInstallationInProgress() {
        return states.entrySet().stream()
                .filter(entry -> entry.getValue().isInitialized())
                .map(entry -> plugins.get(entry.getKey()))
                .filter(java.util.Objects::nonNull)
                .findFirst();
    }

    private record PluginKey(String groupId, String artifactName, String version) {
        private static PluginKey from(PluginDTO plugin) {
            return new PluginKey(plugin.getGroupId(), plugin.getArtifactName(), plugin.getVersion());
        }
    }
}
