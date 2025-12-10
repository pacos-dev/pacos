package org.pacos.core.component.user.view;

import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.AbstractValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.pacos.base.session.UserDTO;
import org.pacos.base.utils.component.TextFieldUtils;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.user.service.UserForm;
import org.pacos.core.component.user.view.event.CreateNewUserEvent;

public record RegisterForm(TextField userName, PasswordField passwordField, PasswordField passwordRepeatField) {
    private static final String ERROR_MESSAGE = "Can't be empty. Min length 3";

    public static RegisterForm create() {
        TextField userName = new TextFieldUtils("Login").withFullWidth();
        PasswordField passwordField = new PasswordField("Password");
        PasswordField passwordRepeatField = new PasswordField("Repeat password");
        return new RegisterForm(userName, passwordField, passwordRepeatField);
    }

    public Binder<UserForm> prepareBinder(UserProxyService proxyService) {
        Binder<UserForm> binder = new Binder<>(UserForm.class);
        binder.setBean(new UserForm());

        binder.forField(userName)
                .withValidator(new StringLengthValidator(ERROR_MESSAGE, 3, 255))
                .withValidator(new AbstractValidator<>("User with this name already exists") {

                    @Override
                    public ValidationResult apply(String s, ValueContext valueContext) {
                        if (proxyService.checkIfLoginNoExists(userName.getValue())) {
                            return ValidationResult.ok();
                        }
                        return ValidationResult.error("This name is already in use");
                    }
                })
                .bind(UserForm::getLogin, UserForm::setLogin);

        binder.forField(passwordField)
                .withValidator(new StringLengthValidator(ERROR_MESSAGE, 3, 255))
                .bind(UserForm::getPassword, UserForm::setPassword);

        binder.forField(passwordRepeatField)
                .withValidator(new StringLengthValidator(ERROR_MESSAGE, 3, 255))
                .withValidator(new AbstractValidator<>("Passwords do not match") {

                    @Override
                    public ValidationResult apply(String s, ValueContext valueContext) {
                        if (s.equals(passwordField.getValue())) {
                            return ValidationResult.ok();
                        }
                        return ValidationResult.error("Passwords do not match");
                    }
                })
                .bind(UserForm::getPassword, UserForm::setPassword);
        return binder;
    }

    public UserDTO createAccount(Binder<UserForm> binder, UserProxyService userProxyService) {
        binder.validate();
        if (binder.isValid()) {
            return CreateNewUserEvent.fireEvent(userProxyService, binder.getBean());
        }
        return null;
    }
}
