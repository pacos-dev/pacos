package org.pacos.core.component.variable.event.user;

import java.util.ArrayList;
import java.util.List;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.base.event.UISystem;
import org.pacos.base.variable.ScopeDefinition;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.component.variable.proxy.UserVariableCollectionProxy;
import org.pacos.core.component.variable.proxy.UserVariableProxy;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;
import org.pacos.core.component.variable.view.PanelVariable;
import org.pacos.core.component.variable.view.user.UserVariableForm;
import org.springframework.transaction.TransactionTimedOutException;
import org.vaadin.addons.variablefield.data.Scope;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SaveUserVariableEventTest {

    @Test
    void whenFireEventOnNotExistingGlobalCollectionThenCreateGlobalCollection() {
        UserVariableSystem systemMock = mockVariableSystem();
        UserVariableCollectionDTO globalCollection = UserVariableCollectionDTO.builder().global(true).build();
        UserVariableForm formMock = mock(UserVariableForm.class);
        UserVariableCollectionProxy proxy = systemMock.getUserVariableCollectionProxy();

        doReturn(globalCollection).when(proxy).createGlobalCollection(2);
        //when
        SaveUserVariableEvent.fireEvent(systemMock, globalCollection, formMock);
        //then
        verify(proxy).createGlobalCollection(2);
    }

    @Test
    void whenFireEventThenSetSavedVariablesOnForm() {
        UserVariableSystem systemMock = mockVariableSystem();
        UserVariableCollectionDTO globalCollection = UserVariableCollectionDTO.builder().global(true).id(1).build();
        UserVariableForm formMock = mock(UserVariableForm.class);
        List<UserVariableDTO> variables = new ArrayList<>();
        variables.add(new UserVariableDTO());
        UserVariableProxy proxy = systemMock.getUserVariableProxy();
        when(proxy.saveForCollection(any(), any())).thenReturn(variables);
        //when
        SaveUserVariableEvent.fireEvent(systemMock, globalCollection, formMock);

        //then
        verify(formMock).setVariables(variables);
        assertEquals(globalCollection.getVariables(), variables);

    }


    @Test
    void whenFireEventThenRefreshVariableModal() {
        UserVariableSystem systemMock = mockVariableSystem();
        UserVariableCollectionDTO globalCollection = UserVariableCollectionDTO.builder().global(true).id(1).build();
        UserVariableForm formMock = mock(UserVariableForm.class);
        List<UserVariableDTO> variables = new ArrayList<>();
        variables.add(new UserVariableDTO());
        UserVariableProxy proxy = systemMock.getUserVariableProxy();
        when(proxy.saveForCollection(any(), any())).thenReturn(variables);
        Scope globalScope = ScopeDefinition.globalCollectionId(2);
        //when
        SaveUserVariableEvent.fireEvent(systemMock, globalCollection, formMock);

        //then
        verify(systemMock.getUiSystem().getVariableManager()).updateVariablesForScope(globalScope, true);
        verify(UISystem.getCurrent()).notify(UserVariableEvent.REFRESH_VARIABLE_LIST, globalCollection);
    }

    @Test
    void whenErrorWhileSaveVariablesThenDoNotThrowException() {
        UserVariableSystem systemMock = mockVariableSystem();
        UserVariableCollectionDTO globalCollection = UserVariableCollectionDTO.builder().global(true).id(1).build();
        UserVariableForm formMock = mock(UserVariableForm.class);

        UserVariableProxy proxy = systemMock.getUserVariableProxy();
        doThrow(TransactionTimedOutException.class).when(proxy).saveForCollection(any(), any());
        //when
        assertDoesNotThrow(() -> SaveUserVariableEvent.fireEvent(systemMock, globalCollection, formMock));
    }

    private UserVariableSystem mockVariableSystem() {
        return spy(new UserVariableSystem(mock(PanelVariable.class),
                VaadinMock.mockSystem(),
                mock(UserVariableCollectionProxy.class),
                mock(UserVariableProxy.class)));
    }

}