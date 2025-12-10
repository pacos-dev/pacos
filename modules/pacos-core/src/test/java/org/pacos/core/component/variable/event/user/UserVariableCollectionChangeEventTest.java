package org.pacos.core.component.variable.event.user;

import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserDTO;
import org.pacos.base.variable.ScopeDefinition;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.system.proxy.AppProxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class UserVariableCollectionChangeEventTest {


    @Test
    void whenCollectionChangedThenUpdateOnUser() {
        int variableCollection = 1;
        int newCollection = 2;
        UserDTO user = new UserDTO(2, "admin", variableCollection);
        UISystem system = VaadinMock.mockSystem(user);
        AppProxy proxy = ProxyMock.appProxyMock();
        UserVariableCollectionDTO variableCollectionDTO = UserVariableCollectionDTO.builder().id(2).build();

        UserProxyService userProxyService = proxy.getUserProxyService();
        //when
        UserVariableCollectionChangeEvent.fireEvent(variableCollectionDTO, system, userProxyService);
        //then
        ArgumentCaptor<UserDTO> userDTOArgumentCaptor = ArgumentCaptor.forClass(UserDTO.class);
        verify(userProxyService).updateUserVariableCollection(userDTOArgumentCaptor.capture());
        assertEquals(newCollection, userDTOArgumentCaptor.getValue().getVariableCollectionId());
        verify(system.getVariableManager()).updateVariablesForScope(ScopeDefinition.userCollection(user.getId()), true);
    }

    @Test
    void whenCollectionIsTheSameThenNoChanges() {
        int variableCollection = 1;
        UISystem system = VaadinMock.mockSystem(new UserDTO(2, "admin", variableCollection));
        AppProxy proxy = ProxyMock.appProxyMock();
        UserVariableCollectionDTO variableCollectionDTO = UserVariableCollectionDTO.builder().id(variableCollection).build();

        UserProxyService userProxyService = proxy.getUserProxyService();
        //when
        UserVariableCollectionChangeEvent.fireEvent(variableCollectionDTO, system, userProxyService);
        //then
        verifyNoInteractions(userProxyService);
    }
}