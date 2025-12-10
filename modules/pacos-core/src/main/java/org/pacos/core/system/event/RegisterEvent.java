
package org.pacos.core.system.event;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.binder.Binder;
import org.pacos.base.session.UserDTO;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.user.service.UserForm;
import org.pacos.core.component.user.view.RegisterForm;

public final class RegisterEvent {

    private RegisterEvent() {
    }

    public static void fireEvent(RegisterForm registerFields, Binder<UserForm> binder,
                                 UserProxyService userProxyService, UI ui) {
        UserDTO userDTO = registerFields.createAccount(binder, userProxyService);
        if (userDTO != null) {
            ui.navigate("login");
            NotificationUtils.success("Account created");
        }

    }

}
