package org.pacos.core.component.installer.view.steps;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.config.VaadinMock;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.installer.service.InstallerService;
import org.pacos.core.component.installer.settings.InstallationMode;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.plugin.event.RefreshAvailablePluginEvent;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.plugin.service.PluginInstallService;
import org.pacos.core.component.plugin.service.PluginService;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class Step6SelectModulesTest {

    private InstallerView installerView;

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();
        InstallerService installerService = mock(InstallerService.class);
        PluginProxy pluginProxy = mock(PluginProxy.class);
        when(installerService.getPluginProxy()).thenReturn(pluginProxy);
        PluginService pluginService = mock(PluginService.class);
        PluginInstallService pluginInstallService = mock(PluginInstallService.class);
        when(pluginProxy.getPluginService()).thenReturn(pluginService);
        when(pluginProxy.getPluginInstallService()).thenReturn(pluginInstallService);
        when(pluginService.findAll()).thenReturn(List.of());
        this.installerView = new InstallerView(installerService, mock(RegistryProxy.class));
        installerView.getSettings().setInstallationMode(InstallationMode.MULTI);
        when(pluginInstallService.isInstallationInProgress()).thenReturn(Boolean.FALSE);
        try (MockedStatic<RefreshAvailablePluginEvent> infoLoader = mockStatic(RefreshAvailablePluginEvent.class)) {
            infoLoader.when(() -> RefreshAvailablePluginEvent.fireEvent(any()))
                    .thenAnswer(inv -> null);
        }

        doNothing().when(VaadinSession.getCurrent()).setAttribute(eq(UserSession.class), any());
        doNothing().when(VaadinSession.getCurrent()).close();
        doNothing().when(UI.getCurrent()).navigate("");
    }

    @Test
    void whenCreateStep6ThenNoException() {
        assertDoesNotThrow(() -> new Step6SelectModules(installerView));
    }

    @Test
    void whenBackBtnEventThenNoException() {
        //when
        Step6SelectModules step = new Step6SelectModules(installerView);
        //then
        assertDoesNotThrow(step::backBtnEvent);
    }

    @Test
    void whenNextBtnEventThenNoException() {
        //when
        Step6SelectModules step = new Step6SelectModules(installerView);
        //then
        assertDoesNotThrow(step::nextBtnEvent);
    }
}