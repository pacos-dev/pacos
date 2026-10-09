package org.pacos.core.component.plugin.manager;

import org.pacos.core.component.plugin.service.PluginStartRequestedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PluginStartRequestedEventHandler {
    private final PluginManager pluginManager;

    public PluginStartRequestedEventHandler(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @EventListener
    public void onPluginStartRequested(PluginStartRequestedEvent event) {
        pluginManager.startPlugin(event.plugin());
    }
}
