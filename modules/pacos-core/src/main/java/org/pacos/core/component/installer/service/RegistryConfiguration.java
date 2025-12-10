package org.pacos.core.component.installer.service;

import org.pacos.core.component.installer.settings.InstallerSettings;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RegistryConfiguration {

    private final RegistryProxy registryProxy;

    @Autowired
    public RegistryConfiguration(RegistryProxy registryProxy) {
        this.registryProxy = registryProxy;
    }

    @Transactional("coreTransactionManager")
    public void configure(InstallerSettings installerSettings) {
        registryProxy.delete(RegistryName.SINGLE_MODE);
        registryProxy.delete(RegistryName.REGISTRATION_PANEL);
        registryProxy.delete(RegistryName.LOG_PERMISSION);
        registryProxy.delete(RegistryName.GUEST_MODE);
        registryProxy.delete(RegistryName.INSTALLED);
        registryProxy.delete(RegistryName.AUTO_UPDATE_ENABLED);


        registryProxy.saveRegistry(RegistryName.SINGLE_MODE, installerSettings.getInstallationMode().isSingle());
        registryProxy.saveRegistry(RegistryName.REGISTRATION_PANEL, installerSettings.getAdditionalSettings().isRegistrationEnabled());
        registryProxy.saveRegistry(RegistryName.LOG_PERMISSION, installerSettings.getAdditionalSettings().isSendingErrorLogAllowed());
        registryProxy.saveRegistry(RegistryName.GUEST_MODE, installerSettings.getAdditionalSettings().isGuestAccountEnabled());
        registryProxy.saveRegistry(RegistryName.AUTO_UPDATE_ENABLED, installerSettings.getAdditionalSettings().isAutoUpdateEnabled());
    }

    public void markAsInstalled() {
        registryProxy.saveRegistry(RegistryName.INSTALLED, "true");
    }
}
