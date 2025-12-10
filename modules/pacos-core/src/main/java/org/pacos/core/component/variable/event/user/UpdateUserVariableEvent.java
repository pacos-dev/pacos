package org.pacos.core.component.variable.event.user;

import java.util.Optional;

import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.variable.ScopeDefinition;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.component.variable.dto.mapper.UserVariableToVariableMapper;
import org.pacos.core.component.variable.proxy.UserVariableCollectionProxy;
import org.pacos.core.component.variable.proxy.UserVariableProxy;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.vaadin.addons.variablefield.data.Scope;
import org.vaadin.addons.variablefield.data.Variable;

public final class UpdateUserVariableEvent {

    private UpdateUserVariableEvent() {
    }

    public static void fireEvent(UserVariableDTO e, UserVariableProxy userVariableProxy, UserVariableCollectionProxy userVariableCollectionProxy) {
        Integer collectionId = e.getCollectionId();
        Optional<UserVariableCollectionDTO> collectionDTO = userVariableCollectionProxy.loadCollection(collectionId);
        if (collectionDTO.isEmpty()) {
            return;
        }
        userVariableProxy.saveVariable(e);
        Variable map = UserVariableToVariableMapper.map(e);

        Scope scope = collectionDTO.get().isGlobal() ? ScopeDefinition.globalCollectionId(UserSession.getCurrent().getUserId()) :
                ScopeDefinition.userCollection(UserSession.getCurrent().getUserId());

        if (e.isEnabled()) {
            UISystem.getCurrent().getVariableManager().updateVariable(scope, map);
        } else {
            UISystem.getCurrent().getVariableManager().removeVariable(scope, map);
        }

        UISystem.getCurrent().notify(UserVariableEvent.REFRESH_VARIABLE, e);
    }

}
