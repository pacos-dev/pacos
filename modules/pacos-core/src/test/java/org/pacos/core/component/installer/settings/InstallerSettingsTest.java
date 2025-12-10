package org.pacos.core.component.installer.settings;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.pacos.config.repository.info.Plugin;

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
    void whenSetCouplerPluginListThenFieldIsUpdated() {
        InstallerSettings installerSettings = new InstallerSettings();
        List<Plugin> plugins = List.of();
        installerSettings.setCouplerPluginList(plugins);
        assertEquals(plugins, installerSettings.getCouplerPluginList());
    }

    @Test
    void whenSetInstallationModeThenFieldIsUpdated() {
        InstallerSettings installerSettings = new InstallerSettings();
        installerSettings.setInstallationMode(InstallationMode.SINGLE);
        assertEquals(InstallationMode.SINGLE, installerSettings.getInstallationMode());
    }

    @Test
    void whenCreatedWithNoArgsConstructorThenInstallationModeAndPluginListAreNull() {
        InstallerSettings installerSettings = new InstallerSettings();
        assertNull(installerSettings.getInstallationMode());
        assertNull(installerSettings.getCouplerPluginList());
    }
}
