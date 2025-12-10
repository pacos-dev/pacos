package org.pacos.core.component.variable.event.user;

import java.util.Optional;

import org.pacos.base.event.UISystem;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

public final class CloneCollectionEvent {
    private CloneCollectionEvent() {
    }

    public static boolean fireEvent(UserVariableSystem system) {
        Optional<UserVariableCollectionDTO> selected = system.getCollectionGrid().getSelectedCollection();
        if (selected.isEmpty()) {
            NotificationUtils.info("No collection selected");
            return false;
        }
        if (selected.get().isGlobal()) {
            NotificationUtils.error("Can't clone global collection");
            return false;
        }
        UserVariableCollectionDTO clonedCollection = system.getUserVariableCollectionProxy()
                .cloneCollection(selected.get());
        system.notify(UserVariableEvent.ADD_NEW_COLLECTION_TO_GRID, clonedCollection);
        UISystem.getCurrent().notify(UserVariableEvent.REFRESH_COLLECTION_LIST);
        return true;
    }
}
