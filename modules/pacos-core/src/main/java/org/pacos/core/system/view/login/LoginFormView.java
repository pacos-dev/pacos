package org.pacos.core.system.view.login;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyDownEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.pacos.base.utils.component.CheckboxUtils;
import org.pacos.base.utils.component.DivUtils;
import org.pacos.core.config.session.UserSessionService;
import org.pacos.core.system.event.LogInEvent;

public class LoginFormView {

    private final UserSessionService userService;

    public LoginFormView(UserSessionService userService) {
        this.userService = userService;
    }

    public void build(DivUtils window) {
        TextField userName = buildUserNameField();
        PasswordField pass = buildPasswordField();
        pass.setValueChangeMode(ValueChangeMode.LAZY);
        pass.addValueChangeListener(event -> pass.setInvalid(false));

        Checkbox rememberMe =
                new CheckboxUtils("Remember me")
                        .withStyle("font-size", "12px");

        Binder<LoginForm> formBinder =
                createBinderForFields(userName, pass, rememberMe);

        String login = "Log in";
        Button loginBtn = new Button(login, e -> tryToLogIn(formBinder, userName, pass));

        loginBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        loginBtn.addClassName("login-btn");

        final ComponentEventListener<KeyDownEvent> onEnterEvent = enterEventListener(formBinder, userName, pass);

        pass.addKeyDownListener(onEnterEvent);
        userName.addKeyDownListener(onEnterEvent);

        String loginField = "login-field";
        window.add(DivUtils.ofClass(loginField).withComponents(userName));
        window.add(DivUtils.ofClass(loginField).withComponents(pass));
        window.add(DivUtils.ofClass(loginField).withComponents(rememberMe));
        window.add(DivUtils.ofClass(loginField).withComponents(loginBtn));
    }

    private ComponentEventListener<KeyDownEvent> enterEventListener(
            Binder<LoginForm> formBinder, TextField userName, PasswordField pass) {
        return event -> {
            if (event.getKey().matches(Key.ENTER.getKeys().getFirst()) || event.getKey()
                    .matches(Key.NUMPAD_ENTER.getKeys().getFirst())) {
                tryToLogIn(formBinder, userName, pass);
            }
        };
    }

    private Binder<LoginForm> createBinderForFields(TextField userName,
            PasswordField pass, Checkbox rememberMe) {
        Binder<LoginForm> formBinder = new Binder<>();
        formBinder.forField(userName)
                .asRequired()
                .bind(LoginForm::getLogin, LoginForm::setLogin);

        formBinder.forField(pass)
                .asRequired()
                .bind(LoginForm::getPassword, LoginForm::setPassword);
        formBinder.forField(rememberMe)
                .bind(LoginForm::getRememberMe, LoginForm::setRememberMe);
        formBinder.setBean(new LoginForm());
        return formBinder;
    }

    private void tryToLogIn(Binder<LoginForm> formBinder, TextField userName, PasswordField pass) {
        pass.setInvalid(false);
        formBinder.validate();
        if (!formBinder.isValid() || !formBinder.writeBeanIfValid(formBinder.getBean())) {
            return;
        }

        if (!LogInEvent.fireEvent(formBinder.getBean(), UI.getCurrent(), userService)) {
            pass.setErrorMessage("Credentials incorrect");
            pass.setInvalid(true);
        }
    }

    private static PasswordField buildPasswordField() {
        PasswordField pass = new PasswordField();
        pass.setPrefixComponent(VaadinIcon.KEY.create());
        pass.setWidthFull();
        pass.setPlaceholder("Password");
        return pass;
    }

    private static TextField buildUserNameField() {
        TextField userName = new TextField();
        userName.setPrefixComponent(VaadinIcon.USER.create());
        userName.setWidthFull();
        userName.setPlaceholder("Username");
        return userName;
    }
}
