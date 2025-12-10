package org.pacos.core.system.event;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.user.view.config.UserGuestSessionConfig;
import org.pacos.core.component.variable.view.plugin.VariablePlugin;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class OpenVariablePluginEventTest {
    @Test
    void whenUserIsNotGuestThenOpenVariablePlugin() {
        VaadinMock.mockSystem();
        RegistryProxy proxy = mock(RegistryProxy.class);
        VariablePlugin pluginProxy = mock(VariablePlugin.class);
        //when
        OpenVariablePluginEvent.fireEvent(UISystem.getCurrent(), pluginProxy, proxy);
        //then
        verify(pluginProxy).open();
    }


    @Test
    void whenUserIsGuestAndRegistrationIsAllowedThenOpenRegistrationPanel() {
        VaadinMock.mockSystem(new UserDTO("guest"));
        RegistryProxy proxy = mock(RegistryProxy.class);
        VariablePlugin pluginProxy = mock(VariablePlugin.class);
        doReturn(true).when(proxy).isRegistrationPanelEnabled();
        //when
        OpenVariablePluginEvent.fireEvent(UISystem.getCurrent(), pluginProxy, proxy);
        //then
        verify(UISystem.getCurrent().getWindowManager()).showWindow(UserGuestSessionConfig.class);
    }

    @Test
    void whenUserIsGuestAndRegistrationIsNotAllowedThenDoNotOpenRegistrationPanel() {
        VaadinMock.mockSystem(new UserDTO("guest"));
        RegistryProxy proxy = mock(RegistryProxy.class);
        doReturn(false).when(proxy).isRegistrationPanelEnabled();
        VariablePlugin pluginProxy = mock(VariablePlugin.class);
        //when
        OpenVariablePluginEvent.fireEvent(UISystem.getCurrent(), pluginProxy, proxy);
        //then
        verifyNoInteractions(UISystem.getCurrent().getWindowManager());
    }
}