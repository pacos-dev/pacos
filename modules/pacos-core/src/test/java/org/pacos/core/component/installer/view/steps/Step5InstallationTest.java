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

class Step5InstallationTest {

    private InstallerView installerView;

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();
        installerView = new InstallerView(mock(InstallerService.class), mock(RegistryProxy.class));
        installerView.getSettings().setInstallationMode(InstallationMode.MULTI);
    }

    @Test
    void whenCreateStep5ThenNoException() {
        assertDoesNotThrow(() -> new Step5Installation(installerView));
    }

    @Test
    void whenNextBtnEventThenNoException() {
        Step5Installation step = new Step5Installation(installerView);

        assertDoesNotThrow(step::nextBtnEvent);
    }

    @Test
    void whenBackBtnEventThenNoException() {
        Step5Installation step = new Step5Installation(installerView);

        assertDoesNotThrow(step::backBtnEvent);
    }
}
