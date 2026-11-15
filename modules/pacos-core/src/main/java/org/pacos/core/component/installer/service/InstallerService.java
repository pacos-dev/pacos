package org.pacos.core.component.installer.service;

import org.pacos.base.session.UserDTO;
import org.pacos.core.component.installer.settings.InstallerSettings;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Coordinates the final installation steps and persists the initial setup owner. */
@Service
public class InstallerService {

    private static final Logger LOG = LoggerFactory.getLogger(InstallerService.class);

    private final RegistryConfiguration registryConfiguration;
    private final UserConfiguration userConfiguration;
    private final DockConfiguration dockConfiguration;
    private final RegistryProxy registryProxy;
    private final PluginProxy pluginProxy;

    @Autowired
    public InstallerService(RegistryConfiguration registryConfiguration,
                            UserConfiguration userConfiguration,
                            DockConfiguration dockConfiguration,
                            RegistryProxy registryProxy,
                            PluginProxy pluginProxy) {
        this.registryConfiguration = registryConfiguration;
        this.userConfiguration = userConfiguration;
        this.dockConfiguration = dockConfiguration;
        this.registryProxy = registryProxy;
        this.pluginProxy = pluginProxy;
    }

    /** Returns the plugin proxy used by the installer UI. */
    public PluginProxy getPluginProxy() {
        return pluginProxy;
    }

    /**
     * Configures the registry, initial user and default dock, then marks PacOS as installed.
     *
     * @param settings validated installation settings
     * @param progressUpdateListener receives installation progress updates
     * @return {@code true} when installation completes
     * @throws IllegalStateException when the initial owner account cannot be resolved
     */
    public boolean configure(InstallerSettings settings, ProgressUpdateListener progressUpdateListener) {
        LOG.info("[IN_PROGRESS] - Database installation");
        progressUpdateListener.progressChanged(0.65);
        LOG.info("[DONE] - Database installation");

        LOG.info("[IN_PROGRESS] - Registry configuration");
        registryConfiguration.configure(settings);
        progressUpdateListener.progressChanged(0.70);
        LOG.info("[DONE] - Registry configuration");

        LOG.info("[IN_PROGRESS] - User configuration");
        UserDTO user = userConfiguration.configure(settings);
        if (user == null) {
            throw new IllegalStateException("Unable to resolve the initial PacOS owner account");
        }
        registryProxy.saveRegistry(RegistryName.SETUP_OWNER_USER_ID, user.getId());
        registryProxy.delete(RegistryName.ONBOARDING_DISMISSED);
        dockConfiguration.configure(user);
        progressUpdateListener.progressChanged(0.95);
        LOG.info("[DONE] - User configuration");

        registryConfiguration.markAsInstalled();
        progressUpdateListener.progressChanged(1);
        return true;
    }
}
