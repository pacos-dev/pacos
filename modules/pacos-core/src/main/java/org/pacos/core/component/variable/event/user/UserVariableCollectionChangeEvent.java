package org.pacos.core.component.variable.event.user;

import java.util.Objects;

import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.base.variable.ScopeDefinition;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;

public class UserVariableCollectionChangeEvent {

    private UserVariableCollectionChangeEvent() {
    }

    public static void fireEvent(UserVariableCollectionDTO variableCollectionDTO, UISystem system, UserProxyService userProxyService) {
        Integer selectedCollection = variableCollectionDTO != null ? variableCollectionDTO.getId() : null;
        UserDTO user = UserSession.getCurrent().getUser();
        if (Objects.equals(user.getVariableCollectionId(), selectedCollection)) {
            return;
        }
        user.setVariableCollectionId(selectedCollection);
        userProxyService.updateUserVariableCollection(user);
        system.getVariableManager().updateVariablesForScope(ScopeDefinition.userCollection(user.getId()), true);
    }
}
