package org.pacos.core.component.variable.event.user;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.event.UISystem;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.proxy.UserVariableCollectionProxy;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AddNewCollectionEventTest {

    @Test
    void whenFireEventThenCreateNewCollectionAndNotifySystemAndUISystem() {
        UserVariableSystem systemMock = mock(UserVariableSystem.class);
        UserVariableCollectionProxy proxyMock = mock(UserVariableCollectionProxy.class);
        UserVariableCollectionDTO collectionMock = mock(UserVariableCollectionDTO.class);
        UISystem uiSystemMock = VaadinMock.mockSystem();

        when(systemMock.getUserVariableCollectionProxy()).thenReturn(proxyMock);
        when(proxyMock.createNewCollection(anyInt())).thenReturn(collectionMock);

        AddNewCollectionEvent.fireEvent(systemMock);

        verify(proxyMock).createNewCollection(2);
        verify(systemMock).notify(UserVariableEvent.ADD_NEW_COLLECTION_TO_GRID, collectionMock);
        verify(uiSystemMock).notify(UserVariableEvent.REFRESH_COLLECTION_LIST);
    }

    @Test
    void whenFireEventAndExceptionOccursThenShowErrorNotification() {
        UserVariableSystem systemMock = mock(UserVariableSystem.class);

        UserVariableCollectionProxy proxyMock = mock(UserVariableCollectionProxy.class);

        when(systemMock.getUserVariableCollectionProxy()).thenReturn(proxyMock);
        when(proxyMock.createNewCollection(anyInt())).thenThrow(new RuntimeException("Error creating collection"));
        try (MockedStatic<NotificationUtils> notifications = mockStatic(NotificationUtils.class)) {

            //when
            AddNewCollectionEvent.fireEvent(systemMock);

            //then
            notifications.verify(()->NotificationUtils.error("Error creating collection"));
        }

    }
}
