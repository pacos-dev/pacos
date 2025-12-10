package org.pacos.core.component.plugin.event;

import org.pacos.config.repository.DependencyResolverException;
import org.pacos.config.repository.RepositoryClient;
import org.pacos.config.repository.data.ModuleConfiguration;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;

public class CheckAvailableSystemUpdateEvent {

    private CheckAvailableSystemUpdateEvent() {
    }

    public static ModuleConfiguration fireEvent(RegistryProxy registryProxy) throws DependencyResolverException {
        ModuleConfiguration moduleList = RepositoryClient.loadModule();
        registryProxy.saveRegistry(RegistryName.AVAILABLE_SYSTEM_VERSION, moduleList.version());
        registryProxy.saveRegistry(RegistryName.LAST_UPDATE_CHECK_TIME, System.currentTimeMillis());
        return moduleList;
    }
}
