package org.pacos.core.component.variable.event.user;

import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

public class AddNewCollectionEvent {
    private AddNewCollectionEvent() {

    }

    public static void fireEvent(UserVariableSystem system) {
        try {
            UserVariableCollectionDTO collectionDTO =
                    system.getUserVariableCollectionProxy().createNewCollection(UserSession.getCurrent().getUserId());
            system.notify(UserVariableEvent.ADD_NEW_COLLECTION_TO_GRID, collectionDTO);
            UISystem.getCurrent().notify(UserVariableEvent.REFRESH_COLLECTION_LIST);
            NotificationUtils.success("New collection created");
        } catch (Exception e) {
            NotificationUtils.error(e.getMessage());
        }
    }
}
