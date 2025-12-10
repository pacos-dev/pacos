package org.pacos.core.component.variable.event.user;

import java.util.Optional;

import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.base.event.UISystem;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.system.proxy.AppProxy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UpdateUserVariableEventTest {

    @Test
    void whenSaveEnabledVariableThenRefreshVariableInScope() {
        UISystem system = VaadinMock.mockSystem();
        AppProxy appProxy = ProxyMock.appProxyMock();
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder().global(false).build();
        when(appProxy.getUserVariableCollectionProxy().loadCollection(any())).thenReturn(Optional.of(collectionDTO));
        UserVariableDTO userVariableDTO = new UserVariableDTO();
        userVariableDTO.setId(1);
        userVariableDTO.setName("name");
        userVariableDTO.setCurrentValue("test");
        userVariableDTO.setEnabled(true);
        doReturn(userVariableDTO).when(appProxy.getUserVariableProxy()).saveVariable(any());
        //when
        UpdateUserVariableEvent.fireEvent(userVariableDTO, appProxy.getUserVariableProxy(), appProxy.getUserVariableCollectionProxy());
        //then
        verify(system.getVariableManager()).updateVariable(any(), any());
        verify(system).notify(UserVariableEvent.REFRESH_VARIABLE, userVariableDTO);
    }

    @Test
    void whenSaveDisabledVariableThenRemoveVariableFromScope() {
        UISystem system = VaadinMock.mockSystem();
        AppProxy appProxy = ProxyMock.appProxyMock();
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder().global(true).build();
        when(appProxy.getUserVariableCollectionProxy().loadCollection(any())).thenReturn(Optional.of(collectionDTO));

        UserVariableDTO userVariableDTO = new UserVariableDTO();
        userVariableDTO.setId(1);
        userVariableDTO.setName("name");
        userVariableDTO.setCurrentValue("test");
        userVariableDTO.setEnabled(false);
        doReturn(userVariableDTO).when(appProxy.getUserVariableProxy()).saveVariable(any());
        //when
        UpdateUserVariableEvent.fireEvent(userVariableDTO, appProxy.getUserVariableProxy(), appProxy.getUserVariableCollectionProxy());
        //then
        verify(system.getVariableManager()).removeVariable(any(), any());
        verify(system).notify(UserVariableEvent.REFRESH_VARIABLE, userVariableDTO);
    }

    @Test
    void whenCollectionNotExistsThenNoChangesOnVariables() {
        UserVariableDTO variable = new UserVariableDTO();
        UISystem system = VaadinMock.mockSystem();
        AppProxy appProxy = ProxyMock.appProxyMock();
        when(appProxy.getUserVariableCollectionProxy().loadCollection(any())).thenReturn(Optional.empty());
        //when
        UpdateUserVariableEvent.fireEvent(variable, appProxy.getUserVariableProxy(), appProxy.getUserVariableCollectionProxy());
        //then
        verifyNoInteractions(system.getVariableManager());
    }
}