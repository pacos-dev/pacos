package org.pacos.core.component.installer.view.steps;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.core.component.installer.settings.InstallationMode;
import org.pacos.core.component.installer.settings.InstallerSettings;
import org.pacos.core.component.installer.view.InstallerView;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

class Step3SetAdminPasswordTest {

    @Test
    void whenCreateStep3ThenNoException() {
        InstallerView installerView = Mockito.mock(InstallerView.class);
        when(installerView.getSettings()).thenReturn(new InstallerSettings());

        assertDoesNotThrow(() -> new Step3SetAdminPassword(installerView));
    }

    @Test
    void whenNextBtnEventThenNoException() {
        InstallerView installerView = Mockito.mock(InstallerView.class);
        InstallerSettings installerSettings = new InstallerSettings();
        installerSettings.setInstallationMode(InstallationMode.SINGLE);
        when(installerView.getSettings()).thenReturn(installerSettings);

        assertDoesNotThrow(() -> new Step3SetAdminPassword(installerView).nextBtnEvent());
    }

    @Test
    void whenBackBtnEventThenNoException() {
        InstallerView installerView = Mockito.mock(InstallerView.class);
        when(installerView.getSettings()).thenReturn(new InstallerSettings());

        assertDoesNotThrow(() -> new Step3SetAdminPassword(installerView).backBtnEvent());
    }
}