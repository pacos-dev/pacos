package org.pacos.core.component.variable.event.user;

import java.util.List;

import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.variable.ScopeDefinition;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;
import org.pacos.core.component.variable.view.user.UserVariableForm;
import org.vaadin.addons.variablefield.data.Scope;

public final class SaveUserVariableEvent {

    private SaveUserVariableEvent() {
    }

    public static void fireEvent(UserVariableSystem system, UserVariableCollectionDTO activeCollection, UserVariableForm form) {
        try {
            createGlobalCollectionIfNotExists(system, activeCollection);

            List<UserVariableDTO> saved = system.getUserVariableProxy()
                    .saveForCollection(activeCollection.getId(), form.getVariables());
            form.setVariables(saved);
            activeCollection.setVariables(saved);
            Scope scope = activeCollection.isGlobal() ?
                    ScopeDefinition.globalCollectionId(UserSession.getCurrent().getUserId()) :
                    ScopeDefinition.userCollection(UserSession.getCurrent().getUserId());

            system.getUiSystem().getVariableManager().updateVariablesForScope(scope, true);
            UISystem.getCurrent().notify(UserVariableEvent.REFRESH_VARIABLE_LIST, activeCollection);

            NotificationUtils.success("Saved");

            FormChangedEvent.fireEvent(false, form);
        } catch (Exception e) {
            NotificationUtils.error(e.getMessage());
        }
    }

    private static void createGlobalCollectionIfNotExists(UserVariableSystem system,
                                                          UserVariableCollectionDTO activeCollection) {
        if (activeCollection.isGlobal() && activeCollection.getId() == null) {
            UserVariableCollectionDTO globalCollection = system.getUserVariableCollectionProxy()
                    .createGlobalCollection(UserSession.getCurrent().getUserId());
            activeCollection.setId(globalCollection.getId());
        }
    }
}
