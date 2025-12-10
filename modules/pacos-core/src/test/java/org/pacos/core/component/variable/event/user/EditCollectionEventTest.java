package org.pacos.core.component.variable.event.user;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EditCollectionEventTest {

    @Test
    void whenFireEventThenNotifySystemWithCollectionDTO() {
        UserVariableSystem systemMock = mock(UserVariableSystem.class);
        UserVariableCollectionDTO collectionMock = mock(UserVariableCollectionDTO.class);

        EditCollectionEvent.fireEvent(collectionMock, systemMock);

        verify(systemMock).notify(UserVariableEvent.OPEN_COLLECTION_VARIABLE_TAB, collectionMock);
    }
}
