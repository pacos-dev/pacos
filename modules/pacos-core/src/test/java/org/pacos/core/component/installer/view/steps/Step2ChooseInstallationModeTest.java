package org.pacos.core.component.installer.view.steps;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.installer.service.InstallerService;
import org.pacos.core.component.installer.settings.InstallationMode;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.registry.proxy.RegistryProxy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class Step2ChooseInstallationModeTest {

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();
    }

    @Test
    void whenCreateStep2ThenNoException() {
        InstallerView installerView = new InstallerView(mock(InstallerService.class), mock(RegistryProxy.class));

        assertDoesNotThrow(() -> new Step2ChooseInstallationMode(installerView));
    }

    @Test
    void whenNextBtnEventForSingleModeThenNoException() {
        InstallerView installerView = new InstallerView(mock(InstallerService.class), mock(RegistryProxy.class));
        Step2ChooseInstallationMode view = new Step2ChooseInstallationMode(installerView);
        view.setModeEvent(InstallationMode.SINGLE);

        assertDoesNotThrow(view::nextBtnEvent);
    }


    @Test
    void whenNextBtnEventForMultiModeThenNoException() {
        InstallerView installerView = new InstallerView(mock(InstallerService.class), mock(RegistryProxy.class));
        Step2ChooseInstallationMode view = new Step2ChooseInstallationMode(installerView);
        view.setModeEvent(InstallationMode.MULTI);

        assertDoesNotThrow(view::nextBtnEvent);
    }

    @Test
    void whenBackBtnEventThenNoException() {
        InstallerView installerView = new InstallerView(mock(InstallerService.class), mock(RegistryProxy.class));
        installerView.getSettings().setInstallationMode(InstallationMode.MULTI);

        assertDoesNotThrow(() -> new Step2ChooseInstallationMode(installerView).backBtnEvent());
    }
}