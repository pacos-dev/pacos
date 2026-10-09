package org.pacos.core.component.plugin.proxy;

import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.service.PluginInstallService;
import org.pacos.core.component.plugin.service.PluginService;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PluginProxy {

    private final PluginInstallService pluginInstallService;
    private final PluginService pluginService;
    private final RegistryProxy registryProxy;
    private final PluginManager pluginManager;
    private final PluginState pluginState;

    @Autowired
    public PluginProxy(PluginInstallService pluginInstallService, PluginService pluginService, RegistryProxy registryProxy, PluginManager pluginManager, PluginState pluginState) {
        this.pluginInstallService = pluginInstallService;
        this.pluginService = pluginService;
        this.registryProxy = registryProxy;
        this.pluginManager = pluginManager;
        this.pluginState = pluginState;
    }

    public PluginInstallService getPluginInstallService() {
        return pluginInstallService;
    }


    public PluginService getPluginService() {
        return pluginService;
    }

    public RegistryProxy getRegistryProxy() {
        return registryProxy;
    }

    public PluginState getPluginState() {
        return pluginState;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }
}
