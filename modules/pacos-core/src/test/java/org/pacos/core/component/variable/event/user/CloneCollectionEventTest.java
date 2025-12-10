package org.pacos.core.component.variable.event.user;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pacos.base.event.UISystem;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.proxy.UserVariableCollectionProxy;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;
import org.pacos.core.component.variable.view.user.UserVariableCollectionGrid;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CloneCollectionEventTest {

    private UserVariableSystem systemMock;
    private UserVariableCollectionGrid collectionGridMock;

    @BeforeEach
    void init() {
        this.systemMock = mock(UserVariableSystem.class);
        this.collectionGridMock = mock(UserVariableCollectionGrid.class);
        when(systemMock.getCollectionGrid()).thenReturn(collectionGridMock);
    }

    @Test
    void whenCollectionNotSelectedThenReturnFalse() {
        when(collectionGridMock.getSelectedCollection()).thenReturn(Optional.empty());

        assertFalse(CloneCollectionEvent.fireEvent(systemMock));
    }

    @Test
    void whenCollectionIsGlobalThenReturnFalse() {
        UserVariableCollectionDTO dto = Mockito.mock(UserVariableCollectionDTO.class);
        when(dto.isGlobal()).thenReturn(true);
        when(collectionGridMock.getSelectedCollection()).thenReturn(Optional.of(dto));

        assertFalse(CloneCollectionEvent.fireEvent(systemMock));
    }

    @Test
    void whenFireEventThenCloneCollectionAndNotifySystemAndPluginToRefreshList() {
        //given
        UserVariableCollectionDTO collectionMock = mock(UserVariableCollectionDTO.class);
        UserVariableCollectionDTO clonedCollectionMock = mock(UserVariableCollectionDTO.class);
        UserVariableCollectionProxy proxyMock = mock(UserVariableCollectionProxy.class);

        when(collectionGridMock.getSelectedCollection()).thenReturn(Optional.of(collectionMock));
        when(systemMock.getUserVariableCollectionProxy()).thenReturn(proxyMock);
        when(proxyMock.cloneCollection(collectionMock)).thenReturn(clonedCollectionMock);

        try (MockedStatic<UISystem> uiSystemMock = mockStatic(UISystem.class)) {
            UISystem ui = mock(UISystem.class);
            uiSystemMock.when(UISystem::getCurrent).thenReturn(ui);

            //when
            boolean result = CloneCollectionEvent.fireEvent(systemMock);

            //then
            assertTrue(result);
            verify(proxyMock).cloneCollection(collectionMock);
            verify(ui).notify(UserVariableEvent.REFRESH_COLLECTION_LIST);
        }
    }
}
