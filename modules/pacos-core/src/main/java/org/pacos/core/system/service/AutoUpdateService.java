package org.pacos.core.system.service;

import java.util.Optional;

import org.pacos.config.repository.data.ModuleConfiguration;
import org.pacos.core.component.plugin.event.CheckAvailableSystemUpdateEvent;
import org.pacos.core.component.plugin.service.PluginUpdateService;
import org.pacos.core.component.plugin.service.PluginVersionChecker;
import org.pacos.core.component.plugin.service.data.PluginUpdateResult;
import org.pacos.core.component.plugin.service.data.PluginsToUpdate;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.system.service.data.SystemUpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutoUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(AutoUpdateService.class);
    private final SystemUpdateService systemUpdateService;
    private final PluginVersionChecker pluginVersionChecker;
    private final RegistryProxy registryProxy;
    private final PluginUpdateService pluginUpdateService;

    @Autowired
    public AutoUpdateService(SystemUpdateService systemUpdateService, PluginVersionChecker pluginVersionChecker, RegistryProxy registryProxy, PluginUpdateService pluginUpdateService) {
        this.systemUpdateService = systemUpdateService;
        this.pluginVersionChecker = pluginVersionChecker;
        this.registryProxy = registryProxy;
        this.pluginUpdateService = pluginUpdateService;
    }

    /**
     * Update the whole system. If force is set, the registry configuration is not taken in to consideration
     */
    @Transactional("coreTransactionManager")
    public Optional<SystemUpdateResult> updateSystem(boolean force) {
        LOG.info("Checking for available changes..");
        ModuleConfiguration systemConfiguration = CheckAvailableSystemUpdateEvent.fireEvent(registryProxy);
        registryProxy.saveRegistry(RegistryName.LAST_UPDATE_CHECK_TIME, System.currentTimeMillis());
        LOG.info("System to update: {}", registryProxy.isSystemToUpdate());
        boolean autoUpdateEnabled = registryProxy.readBoolean(RegistryName.AUTO_UPDATE_ENABLED, false);
        if (autoUpdateEnabled || force) {
            return Optional.of(updateSystem(systemConfiguration));
        } else {
            LOG.info("Auto update disabled. Skipping update.");
        }
        return Optional.empty();
    }

    @Transactional("coreTransactionManager")
    public Optional<PluginUpdateResult> updatePlugins(boolean force) {
        LOG.info("Checking plugins to update");
        PluginsToUpdate pluginsToUpdate = pluginVersionChecker.checkPluginsToUpdate();
        registryProxy.saveRegistry(RegistryName.LAST_UPDATE_CHECK_TIME, System.currentTimeMillis());
        LOG.info("Plugins to update: {}", !pluginsToUpdate.plugins().isEmpty());
        boolean autoUpdateEnabled = registryProxy.readBoolean(RegistryName.AUTO_UPDATE_PLUGIN_ENABLED, false);

        if (autoUpdateEnabled || force) {
            PluginUpdateResult result = updatePlugins(pluginsToUpdate);
            return Optional.of(result);
        } else {
            LOG.info("Auto update plugin disabled. Skipping update.");
        }
        return Optional.empty();
    }

    SystemUpdateResult updateSystem(ModuleConfiguration systemConfiguration) {
        LOG.info("Start updating the system...");
        SystemUpdateResult versionUpdateResult = systemUpdateService.updateSystem(systemConfiguration);
        if (versionUpdateResult.isToUpdate()) {
            registryProxy.saveRegistry(RegistryName.RESTART_REQUIRED, true);
            registryProxy.saveRegistry(RegistryName.LAST_UPDATE_TIME, System.currentTimeMillis());
            registryProxy.saveRegistry(RegistryName.SYSTEM_VERSION, systemConfiguration.version());
        }
        return versionUpdateResult;
    }

    PluginUpdateResult updatePlugins(PluginsToUpdate pluginsToUpdate) {
        LOG.info("Start updating plugins...");
        PluginUpdateResult pluginUpdateResult = pluginUpdateService.updatePlugins(pluginsToUpdate);
        LOG.info(pluginUpdateResult.toString());
        if (!pluginUpdateResult.updated().isEmpty()) {
            registryProxy.saveRegistry(RegistryName.AVAILABLE_PLUGINS_COUNT_TO_UPDATE, 0);
            registryProxy.saveRegistry(RegistryName.LAST_UPDATE_TIME, System.currentTimeMillis());
        }
        return pluginUpdateResult;
    }

    public boolean checkIfUpdateAvailable() {
        PluginsToUpdate pluginsToUpdate = pluginVersionChecker.checkPluginsToUpdate();
        CheckAvailableSystemUpdateEvent.fireEvent(registryProxy);
        return !pluginsToUpdate.plugins().isEmpty() || registryProxy.isSystemToUpdate();
    }
}
