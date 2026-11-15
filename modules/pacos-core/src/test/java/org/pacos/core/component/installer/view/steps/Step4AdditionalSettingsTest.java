package org.pacos.core.component.installer.view.steps;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.installer.service.InstallerService;
import org.pacos.core.component.installer.settings.InstallationMode;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.registry.proxy.RegistryProxy;

import com.vaadin.flow.component.UI;

class Step4AdditionalSettingsTest {

    private InstallerView installerView;

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();
        doNothing().when(UI.getCurrent()).navigate("");
        installerView = new InstallerView(mock(InstallerService.class), mock(RegistryProxy.class));
        installerView.getSettings().setInstallationMode(InstallationMode.MULTI);
    }

    @Test
    void whenCreateStep4ThenNoException() {
        assertDoesNotThrow(() -> new Step4AdditionalSettings(installerView));
    }

    @Test
    void whenNextBtnEventThenNoException() {
        Step4AdditionalSettings step = new Step4AdditionalSettings(installerView);

        assertDoesNotThrow(step::nextBtnEvent);
    }

    @Test
    void whenBackBtnEventForMultiModeThenNoException() {
        Step4AdditionalSettings step = new Step4AdditionalSettings(installerView);

        assertDoesNotThrow(step::backBtnEvent);
    }

    @Test
    void whenBackBtnEventForSingleModeThenNoException() {
        installerView.getSettings().setInstallationMode(InstallationMode.SINGLE);
        Step4AdditionalSettings step = new Step4AdditionalSettings(installerView);

        assertDoesNotThrow(step::backBtnEvent);
    }
}
