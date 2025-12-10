package org.pacos.core.system.service;

import javax.sql.DataSource;
import org.pacos.config.jdbc.ModuleJDBCService;
import org.pacos.config.repository.DependencyResolverException;
import org.pacos.config.repository.PomDependencyResolver;
import org.pacos.config.repository.data.ModuleConfiguration;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.system.service.data.SystemUpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(SystemUpdateService.class);

    private final RegistryProxy registryProxy;
    private final DataSource dataSource;
    private final PluginProxy pluginProxy;


    @Autowired
    public SystemUpdateService(PluginProxy pluginProxy, DataSource dataSource) {
        this.registryProxy = pluginProxy.getRegistryProxy();
        this.pluginProxy = pluginProxy;
        this.dataSource = dataSource;
    }

    @Transactional("coreTransactionManager")
    public SystemUpdateResult updateSystem(ModuleConfiguration systemConfiguration) throws DependencyResolverException {
        SystemUpdateResult systemUpdateResult = new SystemUpdateResult(registryProxy.readRegistryOrDefault(RegistryName.SYSTEM_VERSION, "1.0"), systemConfiguration.version());
        if (systemUpdateResult.isToUpdate()) {
            LOG.info("Updating system...");
            registryProxy.saveRegistry(RegistryName.AVAILABLE_SYSTEM_VERSION, systemConfiguration.version());
            PomDependencyResolver.resolve(systemConfiguration.modules());

            registryProxy.saveRegistry(RegistryName.SYSTEM_VERSION, systemConfiguration.version());
            pluginProxy.getPluginService().disablePluginWithDifferentMajorVersion(new SemanticVersion(systemConfiguration.version()));
            ModuleJDBCService jdbcService = new ModuleJDBCService(dataSource);
            jdbcService.saveModuleConfiguration(systemConfiguration.modules());
        }
        return systemUpdateResult;
    }

}
