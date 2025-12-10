package org.pacos.core.component.variable.event.user;

import java.util.Optional;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.config.impl.ConfirmationWindowConfig;
import org.pacos.base.window.event.OnConfirmEvent;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.proxy.UserVariableCollectionProxy;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;
import org.pacos.core.component.variable.view.PanelVariable;
import org.pacos.core.component.variable.view.user.UserVariableCollectionGrid;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemoveCollectionEventTest {

    private UserVariableSystem systemMock;
    private UserVariableCollectionGrid collectionGridMock;

    @BeforeEach
    void init() {
        this.systemMock = mock(UserVariableSystem.class);
        this.collectionGridMock = mock(UserVariableCollectionGrid.class);
        when(systemMock.getCollectionGrid()).thenReturn(collectionGridMock);
    }

    @Test
    void whenFireEventWithNullCollectionAndNoSelectionThenDoNothing() {
        //given
        when(collectionGridMock.getSelectedCollection()).thenReturn(Optional.empty());

        //when
        assertFalse(RemoveCollectionEvent.fireEvent(systemMock));
    }

    @Test
    void whenFireEventWithGlobalCollectionThenReturnFalse() {
        //given
        UserVariableCollectionDTO collectionMock = mock(UserVariableCollectionDTO.class);
        when(collectionMock.isGlobal()).thenReturn(true);

        when(collectionGridMock.getSelectedCollection()).thenReturn(Optional.of(collectionMock));
        //when
        assertFalse(RemoveCollectionEvent.fireEvent(systemMock));
    }

    @Test
    void whenFireEventWithValidCollectionThenShowConfirmationWindow() {
        //given
        UserVariableCollectionDTO collectionMock = mock(UserVariableCollectionDTO.class);
        when(collectionMock.getName()).thenReturn("Test Collection");
        when(collectionMock.isGlobal()).thenReturn(false);

        PanelVariable panelMock = mock(PanelVariable.class);
        when(systemMock.getPanel()).thenReturn(panelMock);
        when(collectionGridMock.getSelectedCollection()).thenReturn(Optional.of(collectionMock));

        //when
        RemoveCollectionEvent.fireEvent(systemMock);

        //then
        verify(panelMock).addWindow(any(ConfirmationWindowConfig.class));
    }


    @Test
    void whenFireEventWithValidCollectionAddErrorOccurredThenReturnFalse() {
        //given
        UserVariableCollectionDTO collectionMock = mock(UserVariableCollectionDTO.class);
        when(collectionMock.getName()).thenReturn("Test Collection");
        when(collectionMock.isGlobal()).thenReturn(false);

        PanelVariable panelMock = mock(PanelVariable.class);
        when(systemMock.getPanel()).thenReturn(panelMock);

        when(collectionGridMock.getSelectedCollection()).thenReturn(Optional.of(collectionMock));

        doThrow(RuntimeException.class).when(panelMock).addWindow(any());
        //when
        boolean result = RemoveCollectionEvent.fireEvent(systemMock);

        //then
        assertFalse(result);
    }

    @Test
    void whenConfirmEventThenRemoveCollectionAndNotify() {
        //given
        UISystem uiSystemMock = VaadinMock.mockSystem();
        UserVariableSystem systemMock = mock(UserVariableSystem.class);
        UserVariableCollectionDTO collectionMock = mock(UserVariableCollectionDTO.class);
        when(collectionGridMock.getSelectedCollection()).thenReturn(Optional.of(collectionMock));
        UserVariableCollectionProxy proxyMock = mock(UserVariableCollectionProxy.class);
        UserSession.getCurrent().getUser().setVariableCollectionId(1);
        when(collectionMock.getId()).thenReturn(1);
        when(systemMock.getUserVariableCollectionProxy()).thenReturn(proxyMock);

        //when
        OnConfirmEvent confirmEvent = RemoveCollectionEvent.onConfirmEvent(collectionMock, systemMock);
        confirmEvent.confirm();

        //then
        assertNull(UserSession.getCurrent().getUser().getVariableCollectionId());
        verify(proxyMock).removeCollection(collectionMock);
        verify(systemMock).notify(UserVariableEvent.COLLECTION_REMOVED, collectionMock);
        verify(systemMock).notify(UserVariableEvent.CLOSE_TAB, collectionMock);
        verify(uiSystemMock).notify(UserVariableEvent.REFRESH_COLLECTION_LIST);
    }
}
