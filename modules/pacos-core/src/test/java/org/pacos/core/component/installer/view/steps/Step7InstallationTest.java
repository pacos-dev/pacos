package org.pacos.core.component.installer.view.steps;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.config.VaadinMock;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.installer.service.InstallerService;
import org.pacos.core.component.installer.settings.InstallationMode;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.plugin.event.RefreshAvailablePluginEvent;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class Step7InstallationTest {

    private InstallerView installerView;

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();
        this.installerView = new InstallerView(mock(InstallerService.class), mock(RegistryProxy.class));
        installerView.getSettings().setInstallationMode(InstallationMode.MULTI);

        try (MockedStatic<RefreshAvailablePluginEvent> infoLoader = mockStatic(RefreshAvailablePluginEvent.class)) {
            infoLoader.when(() -> RefreshAvailablePluginEvent.fireEvent(any()))
                    .thenAnswer(inv -> null);
        }
        doNothing().when(VaadinSession.getCurrent()).setAttribute(eq(UserSession.class), any());
        doNothing().when(VaadinSession.getCurrent()).close();
        doNothing().when(UI.getCurrent()).navigate("");
    }

    @Test
    void whenCreateStep7ThenNoException() {
        assertDoesNotThrow(() -> new Step7Installation(installerView));
    }


    @Test
    void whenNextBtnEventThenNoException() {
        Step7Installation view = new Step7Installation(installerView);

        assertDoesNotThrow(view::nextBtnEvent);
    }

    @Test
    void whenBackBtnEventThenNoException() {
        Step7Installation view = new Step7Installation(installerView);

        assertDoesNotThrow(view::backBtnEvent);
    }
}