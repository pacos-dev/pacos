package org.pacos.core.component.installer.service;

import org.pacos.base.session.UserDTO;
import org.pacos.core.component.installer.settings.InstallerSettings;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InstallerService {

    private final RegistryConfiguration registryConfiguration;
    private final UserConfiguration userConfiguration;
    private final DockConfiguration dockConfiguration;

    private final PluginProxy pluginProxy;
    private static final Logger LOG = LoggerFactory.getLogger(InstallerService.class);


    @Autowired
    public InstallerService(RegistryConfiguration registryConfiguration,
                            UserConfiguration userConfiguration, DockConfiguration dockConfiguration, PluginProxy pluginProxy) {
        this.registryConfiguration = registryConfiguration;
        this.userConfiguration = userConfiguration;
        this.dockConfiguration = dockConfiguration;
        this.pluginProxy = pluginProxy;
    }

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
        dockConfiguration.configure(user);
        progressUpdateListener.progressChanged(0.95);
        LOG.info("[DONE] - User configuration");

        registryConfiguration.markAsInstalled();
        progressUpdateListener.progressChanged(1);
        return true;
    }

    public PluginProxy getPluginProxy(){
        return pluginProxy;
    }
}
