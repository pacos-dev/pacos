package org.pacos.core.component.variable.event.user;

import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

public final class EditCollectionEvent {
    private EditCollectionEvent() {
    }

    public static void fireEvent(UserVariableCollectionDTO collectionDTO, UserVariableSystem system) {
        system.notify(UserVariableEvent.OPEN_COLLECTION_VARIABLE_TAB, collectionDTO);
    }
}
