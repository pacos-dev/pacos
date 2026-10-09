package org.pacos.core.system.proxy;

import lombok.Getter;
import org.pacos.core.component.dock.proxy.DockServiceProxy;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.variable.proxy.UserVariableCollectionProxy;
import org.pacos.core.component.variable.proxy.UserVariableProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AppProxy {

    private final DockServiceProxy dockServiceProxy;

    private final UserVariableCollectionProxy userVariableCollectionProxy;

    private final UserVariableProxy userVariableProxy;

    private final UserProxyService userProxyService;

    private final RegistryProxy registryProxy;

    private final PluginState pluginState;

    @Autowired
    public AppProxy(DockServiceProxy dockServiceProxy, UserVariableCollectionProxy userVariableCollectionProxy, UserVariableProxy userVariableProxy, UserProxyService userProxyService, RegistryProxy registryProxy, PluginState pluginState) {
        this.dockServiceProxy = dockServiceProxy;
        this.userVariableCollectionProxy = userVariableCollectionProxy;
        this.userVariableProxy = userVariableProxy;
        this.userProxyService = userProxyService;
        this.registryProxy = registryProxy;
        this.pluginState = pluginState;
    }

    public DockServiceProxy getDockServiceProxy() {
        return dockServiceProxy;
    }

    public UserVariableCollectionProxy getUserVariableCollectionProxy() {
        return userVariableCollectionProxy;
    }

    public UserVariableProxy getUserVariableProxy() {
        return userVariableProxy;
    }

    public UserProxyService getUserProxyService() {
        return userProxyService;
    }

    public PluginState getPluginState() {
        return pluginState;
    }

    public RegistryProxy getRegistryProxy() {
        return registryProxy;
    }
}
