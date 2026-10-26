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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginFormView {

    private static final Logger LOG = LoggerFactory.getLogger(LoginFormView.class);

    private final UserSessionService userService;

    public LoginFormView(UserSessionService userService) {
        this.userService = userService;
    }

    public void build(DivUtils window) {
        TextField userName = buildUserNameField();
        PasswordField pass = buildPasswordField();
        // Keep both credentials synchronized before the login button submits the Binder.
        // This avoids browser-side values racing with Vaadin's server-side Binder state.
        userName.setValueChangeMode(ValueChangeMode.EAGER);
        pass.setValueChangeMode(ValueChangeMode.EAGER);
        pass.addValueChangeListener(event -> pass.setInvalid(false));

        Checkbox rememberMe =
                new CheckboxUtils("Remember me")
                        .withStyle("font-size", "12px");

        Binder<LoginForm> formBinder =
                createBinderForFields(userName, pass, rememberMe);

        String login = "Log in";
        Button loginBtn = new Button(login, e -> tryToLogIn(formBinder, pass));

        loginBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        loginBtn.addClassName("login-btn");

        final ComponentEventListener<KeyDownEvent> onEnterEvent = enterEventListener(formBinder, pass);

        pass.addKeyDownListener(onEnterEvent);
        userName.addKeyDownListener(onEnterEvent);

        String loginField = "login-field";
        window.add(DivUtils.ofClass(loginField).withComponents(userName));
        window.add(DivUtils.ofClass(loginField).withComponents(pass));
        window.add(DivUtils.ofClass(loginField).withComponents(rememberMe));
        window.add(DivUtils.ofClass(loginField).withComponents(loginBtn));
    }

    private ComponentEventListener<KeyDownEvent> enterEventListener(
            Binder<LoginForm> formBinder, PasswordField pass) {
        return event -> {
            if (event.getKey().matches(Key.ENTER.getKeys().getFirst()) || event.getKey()
                    .matches(Key.NUMPAD_ENTER.getKeys().getFirst())) {
                tryToLogIn(formBinder, pass);
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

    private void tryToLogIn(Binder<LoginForm> formBinder, PasswordField pass) {
        LoginForm loginForm = formBinder.getBean();
        LOG.info("[LOGIN-DIAG] Handler entered: login={}, passwordPresent={}, passwordLength={}",
                loginForm == null ? null : loginForm.getLogin(),
                loginForm != null && loginForm.getPassword() != null && !loginForm.getPassword().isEmpty(),
                loginForm == null || loginForm.getPassword() == null ? -1 : loginForm.getPassword().length());

        pass.setInvalid(false);
        boolean valid = formBinder.validate().isOk();
        boolean written = valid && formBinder.writeBeanIfValid(loginForm);
        LOG.info("[LOGIN-DIAG] Binder result: valid={}, written={}, login={}, passwordPresent={}, passwordLength={}",
                valid, written,
                loginForm == null ? null : loginForm.getLogin(),
                loginForm != null && loginForm.getPassword() != null && !loginForm.getPassword().isEmpty(),
                loginForm == null || loginForm.getPassword() == null ? -1 : loginForm.getPassword().length());
        if (!valid || !written) {
            LOG.warn("[LOGIN-DIAG] Login stopped by Binder validation");
            return;
        }

        LOG.info("[LOGIN-DIAG] Calling LogInEvent.fireEvent for login={}", loginForm.getLogin());
        boolean authenticated = LogInEvent.fireEvent(loginForm, UI.getCurrent(), userService);
        LOG.info("[LOGIN-DIAG] LogInEvent.fireEvent returned {}", authenticated);
        if (!authenticated) {
            pass.setErrorMessage("Credentials incorrect");
            pass.setInvalid(true);
        }
    }

    private static PasswordField buildPasswordField() {
        PasswordField pass = new PasswordField();
        pass.setPrefixComponent(VaadinIcon.KEY.create());
        pass.setWidthFull();
        pass.setPlaceholder("Password");
        pass.setId("login-password");
        return pass;
    }

    private static TextField buildUserNameField() {
        TextField userName = new TextField();
        userName.setPrefixComponent(VaadinIcon.USER.create());
        userName.setWidthFull();
        userName.setPlaceholder("Username");
        userName.setId("login-username");
        return userName;
    }
}
