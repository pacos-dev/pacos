package org.pacos.core.component.installer.view.steps;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.installer.service.InstallerService;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.registry.proxy.RegistryProxy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class Step1WelcomeTest {


    @Test
    void whenCreateStep1WelcomeThenNoException() {
        VaadinMock.mockSystem();
        InstallerView installerView = new InstallerView(mock(InstallerService.class), mock(RegistryProxy.class));
        assertDoesNotThrow(() -> new Step1Welcome(installerView));
    }

    @Test
    void whenNextBtnEventThenNoException() {
        VaadinMock.mockSystem();
        InstallerView installerView = new InstallerView(mock(InstallerService.class), mock(RegistryProxy.class));
        Step1Welcome step1Welcome = new Step1Welcome(installerView);

        assertDoesNotThrow(step1Welcome::nextBtnEvent);
    }

    @Test
    void whenBackBtnEventThenNoException() {
        VaadinMock.mockSystem();
        InstallerView installerView = new InstallerView(mock(InstallerService.class), mock(RegistryProxy.class));
        Step1Welcome step1Welcome = new Step1Welcome(installerView);

        assertDoesNotThrow(step1Welcome::backBtnEvent);
    }
}
