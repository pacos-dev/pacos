package org.pacos.core.component.user.view;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.user.service.UserForm;

public class RegisterFormLayout {

    private RegisterFormLayout() {

    }

    public static FormLayout build(UISystem uiSystem,
                                   UserGuestSessionPanel desktopWindow,
                                   UserProxyService userProxyService) {
        RegisterForm registerFields = RegisterForm.create();
        Binder<UserForm> binder = registerFields.prepareBinder(userProxyService);
        Button ok = new ButtonUtils("Create account and log out")
                .primaryLayout()
                .withVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL)
                .withClickListener(e -> registerFields.createAccount(binder, userProxyService));

        Button cancel = new ButtonUtils("Cancel")
                .infoLayout()
                .withVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL)
                .withClickListener(e -> uiSystem.notify(ModuleEvent.MODULE_SHUTDOWN, desktopWindow));

        FormLayout formLayout = new FormLayout();
        formLayout.add(new Text("You are in a guest session. "
                + "If you want to set up an account with the possibility of personalization, "
                + "fill out the form and log in using your login and password"));
        formLayout.add(registerFields.userName(), registerFields.passwordField(), registerFields.passwordRepeatField(), cancel, ok);
        formLayout.setColspan(registerFields.userName(), 2);
        formLayout.setColspan(registerFields.passwordField(), 2);
        formLayout.setColspan(registerFields.passwordRepeatField(), 2);

        return formLayout;
    }
}
