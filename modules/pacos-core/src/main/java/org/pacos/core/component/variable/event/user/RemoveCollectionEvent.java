package org.pacos.core.component.variable.event.user;

import java.util.Objects;
import java.util.Optional;

import com.vaadin.flow.component.html.Span;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.component.VerticalLayoutUtils;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.config.impl.ConfirmationWindowConfig;
import org.pacos.base.window.event.OnConfirmEvent;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

public final class RemoveCollectionEvent {

    private RemoveCollectionEvent() {
    }

    public static boolean fireEvent(UserVariableSystem system) {

        try {
            Optional<UserVariableCollectionDTO> selected = system.getCollectionGrid().getSelectedCollection();
            if (selected.isEmpty()) {
                NotificationUtils.info("No collection selected");
                return false;
            }
            if (selected.get().isGlobal()) {
                NotificationUtils.error("Can't remove global collection");
                return false;
            }
            final ConfirmationWindowConfig config = new ConfirmationWindowConfig(onConfirmEvent(selected.get(), system));
            config.setContent(VerticalLayoutUtils.defaults(
                    new Span("Are you sure you want to remove collection '" + selected.get().getName())
            ));
            system.getPanel().addWindow(config);
        } catch (Exception e) {
            NotificationUtils.error(e.getMessage());
            return false;
        }
        return true;
    }

    static OnConfirmEvent onConfirmEvent(UserVariableCollectionDTO collectionDTO, UserVariableSystem system) {
        return () -> {
            if (Objects.equals(UserSession.getCurrent().getUser().getVariableCollectionId(), collectionDTO.getId())) {
                UserSession.getCurrent().getUser().setVariableCollectionId(null);
            }
            system.getUserVariableCollectionProxy().removeCollection(collectionDTO);
            system.notify(UserVariableEvent.COLLECTION_REMOVED, collectionDTO);
            system.notify(UserVariableEvent.CLOSE_TAB, collectionDTO);
            UISystem.getCurrent().notify(UserVariableEvent.REFRESH_COLLECTION_LIST);
            NotificationUtils.success("Collection removed");
            return true;
        };
    }
}
