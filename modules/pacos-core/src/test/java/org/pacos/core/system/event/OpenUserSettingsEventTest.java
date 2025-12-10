package org.pacos.core.system.event;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.user.view.config.UserConfig;
import org.pacos.core.component.user.view.config.UserGuestSessionConfig;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class OpenUserSettingsEventTest {

    @Test
    void whenUserIsNotGuestThenOpenUserConfig() {
        VaadinMock.mockSystem();
        RegistryProxy proxy = mock(RegistryProxy.class);
        //when
        OpenUserSettingsEvent.fireEvent(UISystem.getCurrent(), proxy);
        //then
        verify(UISystem.getCurrent().getWindowManager()).showWindow(UserConfig.class);
    }


    @Test
    void whenUserIsGuestAndRegistrationIsAllowedThenOpenRegistrationPanel() {
        VaadinMock.mockSystem(new UserDTO("guest"));
        RegistryProxy proxy = mock(RegistryProxy.class);
        doReturn(true).when(proxy).isRegistrationPanelEnabled();
        //when
        OpenUserSettingsEvent.fireEvent(UISystem.getCurrent(), proxy);
        //then
        verify(UISystem.getCurrent().getWindowManager()).showWindow(UserGuestSessionConfig.class);
    }

    @Test
    void whenUserIsGuestAndRegistrationIsNotAllowedThenDoNotOpenRegistrationPanel() {
        VaadinMock.mockSystem(new UserDTO("guest"));
        RegistryProxy proxy = mock(RegistryProxy.class);
        doReturn(false).when(proxy).isRegistrationPanelEnabled();
        //when
        OpenUserSettingsEvent.fireEvent(UISystem.getCurrent(), proxy);
        //then
        verifyNoInteractions(UISystem.getCurrent().getWindowManager());
    }
}