package org.pacos.core.component.installer.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class InstallerSettingsTest {

    @Test
    void whenCreatedWithNoArgsConstructorThenAccountDataAndAdditionalSettingsAreInitialized() {
        InstallerSettings installerSettings = new InstallerSettings();
        assertNotNull(installerSettings.getAccountData());
        assertNotNull(installerSettings.getAdditionalSettings());
    }

    @Test
    void whenSetInstallationModeThenFieldIsUpdated() {
        InstallerSettings installerSettings = new InstallerSettings();
        installerSettings.setInstallationMode(InstallationMode.SINGLE);
        assertEquals(InstallationMode.SINGLE, installerSettings.getInstallationMode());
    }

    @Test
    void whenCreatedWithNoArgsConstructorThenInstallationModeIsNull() {
        InstallerSettings installerSettings = new InstallerSettings();
        assertNull(installerSettings.getInstallationMode());
    }
}
