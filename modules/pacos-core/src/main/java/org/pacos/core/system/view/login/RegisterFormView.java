package org.pacos.core.system.view.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.component.DivUtils;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.user.service.UserForm;
import org.pacos.core.component.user.view.RegisterForm;
import org.pacos.core.system.event.RegisterEvent;

public class RegisterFormView {

    private RegisterFormView() {
    }

    public static Div build(UserProxyService userProxyService) {
        Div div = new Div();
        div.setClassName("input-form");

        RegisterForm registerFields = RegisterForm.create();
        Binder<UserForm> binder = registerFields.prepareBinder(userProxyService);

        Button registerBtn = new Button("Register account", event ->
                RegisterEvent.fireEvent(registerFields, binder, userProxyService, UI.getCurrent()));

        registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        registerBtn.addClassName("login-btn");

        Button backBtn = new ButtonUtils("Back to login page")
                .withThemes(ButtonVariant.LUMO_PRIMARY.getVariantName())
                .withClassName("login-invert-btn")
                .withClickListener(e -> div.getUI().ifPresent(ui -> ui.navigate("login")));

        styleNameField(registerFields.userName());
        stylePasswordField(registerFields.passwordField(), "Password");
        stylePasswordField(registerFields.passwordRepeatField(), "Repeat password");

        String loginField = "login-field";
        div.add(DivUtils.ofClass(loginField).withComponents(registerFields.userName()));
        div.add(DivUtils.ofClass(loginField).withComponents(registerFields.passwordField()));
        div.add(DivUtils.ofClass(loginField).withComponents(registerFields.passwordRepeatField()));
        div.add(DivUtils.ofClass(loginField).withComponents(registerBtn));
        div.add(DivUtils.ofClass(loginField).withComponents(backBtn));

        return div;
    }

    private static void stylePasswordField(PasswordField pass, String placeholder) {
        pass.setPrefixComponent(VaadinIcon.KEY.create());
        pass.setPlaceholder(placeholder);
        pass.setWidthFull();
        pass.setLabel(null);
    }

    private static void styleNameField(TextField userName) {
        userName.setPrefixComponent(VaadinIcon.USER.create());
        userName.setPlaceholder("Login");
        userName.setLabel(null);
    }
}
