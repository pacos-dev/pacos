package org.pacos.core.component.installer.settings;

import org.pacos.core.component.user.service.UserForm;

public class InstallerSettings {
    private InstallationMode installationMode;
    private final UserForm accountData;
    private final AdditionalSettings additionalSettings;

    public InstallerSettings() {
        this.accountData = new UserForm();
        this.additionalSettings = new AdditionalSettings();
    }

    public InstallationMode getInstallationMode() {
        return installationMode;
    }

    public void setInstallationMode(InstallationMode installationMode) {
        this.installationMode = installationMode;
    }

    public UserForm getAccountData() {
        return accountData;
    }

    public AdditionalSettings getAdditionalSettings() {
        return additionalSettings;
    }
}
