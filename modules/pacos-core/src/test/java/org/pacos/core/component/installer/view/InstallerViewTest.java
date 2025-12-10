package org.pacos.core.component.installer.view;

import java.util.Optional;

import com.vaadin.flow.router.BeforeEnterEvent;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.installer.service.InstallerService;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.system.view.DesktopView;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InstallerViewTest {

    @Test
    void whenInstalledThenRedirectToDesktopPage() {
        VaadinMock.mockSystem();
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        when(registryProxy.readRegistry(RegistryName.INSTALLED)).thenReturn(Optional.of("true"));
        InstallerView installerView = new InstallerView(mock(InstallerService.class), registryProxy);
        BeforeEnterEvent enterEvent = mock(BeforeEnterEvent.class);
        //when
        installerView.beforeEnter(enterEvent);
        //then
        verify(enterEvent).forwardTo(DesktopView.class);
    }

    @Test
    void whenNotInstalledThenDoNotRedirect() {
        VaadinMock.mockSystem();
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        when(registryProxy.readRegistry(RegistryName.INSTALLED)).thenReturn(Optional.of("false"));
        InstallerView installerView = new InstallerView(mock(InstallerService.class), registryProxy);
        BeforeEnterEvent enterEvent = mock(BeforeEnterEvent.class);
        //when
        installerView.beforeEnter(enterEvent);
        //then
        verifyNoInteractions(enterEvent);
    }

    @Test
    void whenInstalledFlagNotFoundThenDoNotRedirect() {
        VaadinMock.mockSystem();
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        when(registryProxy.readRegistry(RegistryName.INSTALLED)).thenReturn(Optional.empty());
        InstallerView installerView = new InstallerView(mock(InstallerService.class), registryProxy);
        BeforeEnterEvent enterEvent = mock(BeforeEnterEvent.class);
        //when
        installerView.beforeEnter(enterEvent);
        //then
        verifyNoInteractions(enterEvent);
    }

}