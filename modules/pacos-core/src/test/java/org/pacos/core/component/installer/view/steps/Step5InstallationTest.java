package org.pacos.core.component.installer.view.steps;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.installer.service.InstallerService;
import org.pacos.core.component.installer.settings.InstallationMode;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.registry.proxy.RegistryProxy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.timeout;

class Step5InstallationTest {

    private InstallerView installerView;
    private InstallerService installerService;

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();
        installerService = mock(InstallerService.class);
        installerView = new InstallerView(installerService, mock(RegistryProxy.class));
        doNothing().when(VaadinSession.getCurrent()).setAttribute(eq(UserSession.class), any());
        doNothing().when(VaadinSession.getCurrent()).close();
        installerView.getSettings().setInstallationMode(InstallationMode.MULTI);
    }

    @Test
    void whenCreateStep5ThenNoException() {
        assertDoesNotThrow(() -> new Step5Installation(installerView));
    }

    @Test
    void whenStep5IsAttachedThenInstallationStartsAutomatically() {
        Step5Installation step = new Step5Installation(installerView);
        UI.getCurrent().add(installerView);
        installerView.setView(step);

        verify(installerService, timeout(1000)).configure(eq(installerView.getSettings()), any());
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
