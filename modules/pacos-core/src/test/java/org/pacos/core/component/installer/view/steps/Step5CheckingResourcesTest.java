package org.pacos.core.component.installer.view.steps;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.config.VaadinMock;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.installer.service.InstallerService;
import org.pacos.core.component.installer.settings.InstallationMode;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.plugin.loader.PluginInfoLoader;
import org.pacos.core.component.plugin.loader.PluginRefreshListener;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class Step5CheckingResourcesTest {


    private ArgumentCaptor<PluginRefreshListener> refresPluginsCaptor;
    private InstallerView installerView;

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();

        this.installerView = new InstallerView(mock(InstallerService.class), mock(RegistryProxy.class));
        installerView.getSettings().setInstallationMode(InstallationMode.MULTI);

        this.refresPluginsCaptor = ArgumentCaptor.forClass(PluginRefreshListener.class);

        try (MockedStatic<PluginInfoLoader> infoLoader = mockStatic(PluginInfoLoader.class)) {
            infoLoader.when(() -> PluginInfoLoader.refreshPluginList(refresPluginsCaptor.capture()))
                    .thenAnswer(inv -> null);
        }
        doNothing().when(VaadinSession.getCurrent()).setAttribute(eq(UserSession.class), any());
        doNothing().when(VaadinSession.getCurrent()).close();
        doNothing().when(UI.getCurrent()).navigate("");
    }

    @Test
    void whenPluginListNotLoadedThenDisplayErrorContent() {
        //when
        Step5CheckingResources step = spy(new Step5CheckingResources(installerView));
        step.refreshPluginListView(null);
        verify(step).displayErrorCantLoad();
    }

    @Test
    void whenBackBtnEventThenNoException() {
        //when
        Step5CheckingResources step = new Step5CheckingResources(installerView);
        //then
        assertDoesNotThrow(step::backBtnEvent);
    }

    @Test
    void whenNextBtnEventThenNoException() {
        //when
        Step5CheckingResources step = new Step5CheckingResources(installerView);
        //then
        assertDoesNotThrow(step::nextBtnEvent);
    }
}
