package org.pacos.core.component.variable.event.global;

import org.junit.jupiter.api.Test;
import org.pacos.common.view.tab.TabMonitoring;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.proxy.SystemVariableProxy;
import org.pacos.core.component.variable.system.global.GlobalVariableEvent;
import org.pacos.core.component.variable.system.global.GlobalVariableSystem;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SaveGlobalVariableEventTest {

    @Test
    void whenFireEventThenSaveVariableAndUpdateTabAndNotifyAndShowSuccess() {
        //given
        TabMonitoring tabMock = mock(TabMonitoring.class);
        SystemVariableDTO variableDtoMock = mock(SystemVariableDTO.class);
        GlobalVariableSystem systemMock = mock(GlobalVariableSystem.class);
        SystemVariableProxy proxyMock = mock(SystemVariableProxy.class);

        when(systemMock.getSystemVariableProxy()).thenReturn(proxyMock);
        when(variableDtoMock.getName()).thenReturn("testVariable");

        //when
        SaveGlobalVariableEvent.fireEvent(tabMock, variableDtoMock, systemMock);

        //then
        verify(proxyMock).save(variableDtoMock);
        verify(tabMock).updateLabel("testVariable");
        verify(systemMock).notify(GlobalVariableEvent.REFRESH_ENTRY, variableDtoMock);
    }
}
