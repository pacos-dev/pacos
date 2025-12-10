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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RenameCollectionEventTest {

    @Test
    void whenFireEventWithNullCollectionThenDoNothing() {
        //when
        UserVariableSystem systemMock = mock(UserVariableSystem.class);

        //when
        RenameCollectionEvent.fireEvent(null, systemMock);

        //then
        verifyNoInteractions(systemMock);
    }

    @Test
    void whenFireEventWithValidCollectionThenUpdateAndNotify() {
        //when
        UISystem uiSystemMock = VaadinMock.mockSystem();
        UserVariableSystem systemMock = mock(UserVariableSystem.class);
        UserVariableCollectionDTO collectionMock = mock(UserVariableCollectionDTO.class);
        UserVariableCollectionProxy proxyMock = mock(UserVariableCollectionProxy.class);
        when(systemMock.getUserVariableCollectionProxy()).thenReturn(proxyMock);
        when(systemMock.getUiSystem()).thenReturn(uiSystemMock);
        //when
        RenameCollectionEvent.fireEvent(collectionMock, systemMock);

        //then
        verify(proxyMock).update(collectionMock);
        verify(systemMock).notify(UserVariableEvent.REFRESH_COLLECTION_NAME, collectionMock);
        verify(uiSystemMock).notify(UserVariableEvent.REFRESH_COLLECTION_LIST, collectionMock);
        // Verify success notification if possible.
    }

    @Test
    void whenFireEventAndExceptionOccursThenShowErrorNotification() {
        //when
        UserVariableSystem systemMock = mock(UserVariableSystem.class);
        UserVariableCollectionDTO collectionMock = mock(UserVariableCollectionDTO.class);
        UserVariableCollectionProxy proxyMock = mock(UserVariableCollectionProxy.class);

        when(systemMock.getUserVariableCollectionProxy()).thenReturn(proxyMock);
        doThrow(new RuntimeException("Error updating collection")).when(proxyMock).update(collectionMock);
        try (MockedStatic<NotificationUtils> notifications = mockStatic(NotificationUtils.class)) {

            //when
            RenameCollectionEvent.fireEvent(collectionMock, systemMock);

            //then
            notifications.verify(()->NotificationUtils.error("Error updating collection"));
        }

    }
}
