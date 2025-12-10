package org.pacos.core.component.installer.view.steps;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.core.component.installer.settings.InstallationMode;
import org.pacos.core.component.installer.settings.InstallerSettings;
import org.pacos.core.component.installer.view.InstallerView;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

class Step4AdditionalSettingsTest {
    @Test
    void whenCreateStep4ThenNoException() {
        InstallerView installerView = Mockito.mock(InstallerView.class);
        InstallerSettings installerSettings = new InstallerSettings();
        installerSettings.setInstallationMode(InstallationMode.MULTI);
        when(installerView.getSettings()).thenReturn(installerSettings);

        assertDoesNotThrow(() -> new Step4AdditionalSettings(installerView));
    }


    @Test
    void whenNextBtnEventThenNoException() {
        InstallerView installerView = Mockito.mock(InstallerView.class);
        InstallerSettings installerSettings = new InstallerSettings();
        installerSettings.setInstallationMode(InstallationMode.MULTI);
        when(installerView.getSettings()).thenReturn(installerSettings);

        assertDoesNotThrow(() -> new Step4AdditionalSettings(installerView).nextBtnEvent());
    }

    @Test
    void whenBackBtnEventForMultiModeThenNoException() {
        InstallerView installerView = Mockito.mock(InstallerView.class);
        InstallerSettings installerSettings = new InstallerSettings();
        installerSettings.setInstallationMode(InstallationMode.MULTI);
        when(installerView.getSettings()).thenReturn(installerSettings);

        assertDoesNotThrow(() -> new Step4AdditionalSettings(installerView).backBtnEvent());
    }

    @Test
    void whenBackBtnEventForSingleModeThenNoException() {
        InstallerView installerView = Mockito.mock(InstallerView.class);
        InstallerSettings installerSettings = new InstallerSettings();
        installerSettings.setInstallationMode(InstallationMode.SINGLE);
        when(installerView.getSettings()).thenReturn(installerSettings);

        assertDoesNotThrow(() -> new Step4AdditionalSettings(installerView).backBtnEvent());
    }
}