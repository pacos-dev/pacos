package org.pacos.core.component.plugin.manager;

import java.util.concurrent.CompletableFuture;

import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.service.PluginService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Maintains the plugin catalog and exposes a facade for runtime operations.
 */
@Component
public class PluginManager {

    private final PluginService pluginService;
    private final PluginState pluginState;
    private final PluginLifecycle pluginLifecycle;

    public PluginManager(PluginService pluginService, PluginState pluginState, PluginLifecycle pluginLifecycle) {
        this.pluginService = pluginService;
        this.pluginState = pluginState;
        this.pluginLifecycle = pluginLifecycle;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializePluginsOnApplicationReadyEvent() {
        pluginLifecycle.initializeResources();
        pluginService.findNotRemovedPlugin().forEach(this::addPlugin);
        // Preserve startup readiness without filling the bounded lifecycle executor queue.
        pluginService.findEnabledPlugin().forEach(plugin -> startPlugin(plugin).join());
    }

    /**
     * Adds plugin metadata during startup and after installation without starting it.
     */
    public void addPlugin(PluginDTO plugin) {
        pluginState.addPlugin(plugin);
    }

    /**
     * Removes plugin resources and its catalog entry during update or uninstall.
     */
    public void removePlugin(PluginDTO plugin) {
        pluginLifecycle.removePlugin(plugin, () -> pluginState.removePlugin(plugin));
    }

    public CompletableFuture<Boolean> stopPlugin(PluginDTO plugin) {
        return pluginLifecycle.stopPlugin(plugin);
    }

    public CompletableFuture<Boolean> startPlugin(PluginDTO plugin) {
        return pluginLifecycle.startPlugin(plugin);
    }
}
