package org.pacos.core.system.proxy;

import lombok.Getter;
import org.pacos.core.component.dock.proxy.DockServiceProxy;
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

    @Autowired
    public AppProxy(DockServiceProxy dockServiceProxy, UserVariableCollectionProxy userVariableCollectionProxy, UserVariableProxy userVariableProxy, UserProxyService userProxyService, RegistryProxy registryProxy) {
        this.dockServiceProxy = dockServiceProxy;
        this.userVariableCollectionProxy = userVariableCollectionProxy;
        this.userVariableProxy = userVariableProxy;
        this.userProxyService = userProxyService;
        this.registryProxy = registryProxy;
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

    public RegistryProxy getRegistryProxy() {
        return registryProxy;
    }
}
