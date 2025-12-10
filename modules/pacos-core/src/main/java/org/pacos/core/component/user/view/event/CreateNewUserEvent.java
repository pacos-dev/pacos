package org.pacos.core.component.user.view.event;

import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.user.service.UserForm;
import org.pacos.core.config.session.UserSessionService;

public class CreateNewUserEvent {

    private CreateNewUserEvent() {
    }

    public static UserDTO fireEvent(UserProxyService userProxyService, UserForm form) {
        if (userProxyService.checkIfLoginNoExists(form.getLogin())) {
            UserDTO userDTO = userProxyService.createAccount(form);
            if(UserSession.getCurrent()!=null) {
                UserSessionService.logOut();
            }
            return userDTO;
        }
        return null;
    }
}
