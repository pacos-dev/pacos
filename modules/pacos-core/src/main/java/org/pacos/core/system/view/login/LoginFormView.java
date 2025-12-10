package org.pacos.core.system.view.login;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyDownEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.AbstractValidator;
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

        Checkbox rememberMe =
                new CheckboxUtils("Remember me")
                        .withStyle("font-size", "18px");

        Binder<LoginForm> formBinder =
                createBinderForFields(userName, pass, rememberMe);

        String login = "Log in";
        Button loginBtn = new Button(login, e -> tryToLogIn(formBinder));

        loginBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        loginBtn.addClassName("login-btn");

        final ComponentEventListener<KeyDownEvent> onEnterEvent = enterEventListener(formBinder);

        pass.addKeyDownListener(onEnterEvent);
        userName.addKeyDownListener(onEnterEvent);

        String loginField = "login-field";
        window.add(DivUtils.ofClass(loginField).withComponents(userName));
        window.add(DivUtils.ofClass(loginField).withComponents(pass));
        window.add(DivUtils.ofClass(loginField).withComponents(rememberMe));
        window.add(DivUtils.ofClass(loginField).withComponents(loginBtn));
    }

    private ComponentEventListener<KeyDownEvent> enterEventListener(Binder<LoginForm> formBinder) {
        return event -> {
            if (event.getKey().matches(Key.ENTER.getKeys().getFirst()) || event.getKey()
                    .matches(Key.NUMPAD_ENTER.getKeys().getFirst())) {
                tryToLogIn(formBinder);
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
                .withValidator(credentialsValidator(userName))
                .bind(LoginForm::getPassword, LoginForm::setPassword);
        formBinder.forField(rememberMe)
                .bind(LoginForm::getRememberMe, LoginForm::setRememberMe);
        formBinder.setBean(new LoginForm());
        return formBinder;
    }

    private AbstractValidator<String> credentialsValidator(TextField userName) {
        return new AbstractValidator<>("Credentials incorrect") {

            @Override
            public ValidationResult apply(String s, ValueContext valueContext) {
                if (userService.isValidCredentials(new LoginForm(userName.getValue(), s))) {
                    return ValidationResult.ok();
                }
                return ValidationResult.error("Credentials incorrect");
            }
        };
    }

    private void tryToLogIn(Binder<LoginForm> formBinder) {
        formBinder.validate();
        if (formBinder.isValid() && formBinder.writeBeanIfValid(formBinder.getBean())) {
            LogInEvent.fireEvent(formBinder.getBean(), UI.getCurrent(), userService);
        }
    }

    private static PasswordField buildPasswordField() {
        PasswordField pass = new PasswordField();
        pass.setPrefixComponent(VaadinIcon.KEY.create());
        pass.setWidth(450, Unit.PIXELS);
        pass.setPlaceholder("Password");
        return pass;
    }

    private static TextField buildUserNameField() {
        TextField userName = new TextField();
        userName.setPrefixComponent(VaadinIcon.USER.create());
        userName.setWidth(450, Unit.PIXELS);
        userName.setPlaceholder("Username");
        return userName;
    }
}
