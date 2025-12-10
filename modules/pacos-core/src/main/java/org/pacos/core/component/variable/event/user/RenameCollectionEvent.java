package org.pacos.core.component.variable.event.user;

import org.pacos.base.event.UISystem;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

public final class RenameCollectionEvent {

    private RenameCollectionEvent() {
    }

    public static void fireEvent(UserVariableCollectionDTO collectionDTO, UserVariableSystem system) {
        if (collectionDTO == null) {
            return;
        }
        try {
            system.getUserVariableCollectionProxy().update(collectionDTO);
            system.notify(UserVariableEvent.REFRESH_COLLECTION_NAME, collectionDTO);
            UISystem.getCurrent().notify(UserVariableEvent.REFRESH_COLLECTION_LIST, collectionDTO);
            NotificationUtils.success("Saved");
        } catch (Exception e) {
            NotificationUtils.error(e.getMessage());
        }
    }
}
