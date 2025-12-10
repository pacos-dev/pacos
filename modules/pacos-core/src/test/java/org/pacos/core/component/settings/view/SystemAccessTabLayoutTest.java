package org.pacos.core.component.settings.view;

import org.config.ProxyMock;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

class SystemAccessTabLayoutTest {

    @Test
    void whenInitializeViewThenNoException() {
        RegistryProxy proxy = ProxyMock.registryProxy();
        doReturn(true).when(proxy).isRegistrationPanelEnabled();
        doReturn(false).when(proxy).isGuestMode();

        //then
        assertDoesNotThrow(() -> new SystemAccessTabLayout(proxy));
    }

    @Test
    void whenSaveRegistryThenCallRegistryProxy() {
        RegistryProxy proxy = ProxyMock.registryProxy();
        doReturn(true).when(proxy).isRegistrationPanelEnabled();
        doReturn(false).when(proxy).isGuestMode();
        //when
        new SystemAccessTabLayout(proxy).saveRegistryValue(true, RegistryName.GUEST_MODE, "guest");
        //then
        verify(proxy).saveRegistry(RegistryName.GUEST_MODE, true);

    }
}