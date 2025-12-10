package org.pacos.core.component.installer.view.steps;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.installer.view.steps.helper.BoxContent;
import org.pacos.core.component.user.service.UserForm;

@SuppressWarnings("squid:S110")
public class Step3SetAdminPassword extends Box {
    private final Button nextButton;
    private final Binder<UserForm> binder;

    public Step3SetAdminPassword(InstallerView installerView) {
        super(installerView, "Pac OS - Configuration");
        setProgressBarValue(0.2);
        BoxContent content = new BoxContent();
        content.addTextLine("Configure admin account");
        add(content);

        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setWidthFull();

        TextField login = new TextField("Login");
        login.setValueChangeMode(ValueChangeMode.EAGER);

        login.setWidthFull();
        formLayout.add(login);

        PasswordField password = new PasswordField("Password");
        PasswordField repeatPassword = new PasswordField("Repeat password");
        this.binder = new Binder<>();
        binder.setBean(installerView.getSettings().getAccountData());

        String errorMessage = "Max allowed length is 255";
        binder.forField(login)
                .asRequired()
                .withValidator(new StringLengthValidator(errorMessage, 0, 200))
                .bind(UserForm::getLogin, UserForm::setLogin);
        binder.forField(password)
                .asRequired()
                .withValidator(new StringLengthValidator(errorMessage, 0, 200))
                .bind(UserForm::getPassword, UserForm::setPassword);


        binder.forField(repeatPassword)
                .asRequired()
                .withValidator(new StringLengthValidator(errorMessage, 0, 200))
                .withValidator((Validator<String>) (s, valueContext) -> {
                    if (!password.getValue().isEmpty() && !password.getValue().equals(repeatPassword.getValue())) {
                        return ValidationResult.error("Given passwords are different");
                    }
                    return ValidationResult.ok();
                })
                .bind(UserForm::getPassword, UserForm::setPassword);


        password.setValueChangeMode(ValueChangeMode.EAGER);
        password.setRequiredIndicatorVisible(true);
        password.setWidthFull();

        this.nextButton = addNextBtn();

        password.addValueChangeListener(e -> nextButton.setEnabled(binder.isValid()));
        formLayout.add(password);


        repeatPassword.setValueChangeMode(ValueChangeMode.EAGER);
        repeatPassword.addValueChangeListener(e -> nextButton.setEnabled(binder.isValid()));

        login.addValueChangeListener(e -> nextButton.setEnabled(binder.isValid()));
        repeatPassword.setWidthFull();
        formLayout.add(repeatPassword);
        content.add(formLayout);

        addBackBtn();

        nextButton.setEnabled(binder.isValid());
    }

    @Override
    protected void nextBtnEvent() {
        binder.writeBeanIfValid(installerView.getSettings().getAccountData());
        installerView.setView(new Step4AdditionalSettings(installerView));

    }

    @Override
    protected void backBtnEvent() {
        installerView.setView(new Step2ChooseInstallationMode(installerView));
    }
}
