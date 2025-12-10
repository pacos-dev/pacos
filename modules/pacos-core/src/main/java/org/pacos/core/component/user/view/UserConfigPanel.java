package org.pacos.core.component.user.view;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.AbstractValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.component.DivUtils;
import org.pacos.base.utils.component.ParagraphUtils;
import org.pacos.base.utils.component.SpanUtils;
import org.pacos.base.window.DesktopWindow;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.user.service.ChangePasswordForm;
import org.pacos.core.component.user.view.config.UserConfig;
import org.pacos.core.component.user.view.event.ChangePasswordEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class UserConfigPanel extends DesktopWindow {

    public UserConfigPanel(UserConfig moduleConfig, UserProxyService userProxyService) {
        super(moduleConfig);
        allowCloseOnEsc();
        setSize(650, 350);
        getWindowHeader().getRight().removeAll();
        setResizable(false);
        setModal(true);
        add(new ParagraphUtils(UserSession.getCurrent().getUserName()).withStyle("text-weight", "bold")
                .withMarginAuto());
        Avatar avatar = new Avatar(UserSession.getCurrent().getUserName());
        avatar.setColorIndex(UserSession.getCurrent().getUserId());
        avatar.setThemeName("xlarge");
        add(new DivUtils().withMarginAuto().withComponents(avatar));

        PasswordField currentPassword = new PasswordField();
        add(new DivUtils().withMarginAuto().withComponents(generateLabel("Current password"), currentPassword));
        PasswordField newPassword = new PasswordField();
        add(new DivUtils().withMarginAuto().withComponents(generateLabel("New password"), newPassword));
        PasswordField repeatPassword = new PasswordField();
        add(new DivUtils().withMarginAuto().withComponents(generateLabel("Repeat new password"), repeatPassword));

        Binder<ChangePasswordForm> changePasswordFormBinder = new Binder<>();
        changePasswordFormBinder.setBean(new ChangePasswordForm());

        final String errorMessage = "Can't be empty. Min length 3";
        changePasswordFormBinder.forField(currentPassword)
                .withValidator(new StringLengthValidator(errorMessage, 3, 255))
                .bind(ChangePasswordForm::getCurrentPassword, ChangePasswordForm::setCurrentPassword);

        changePasswordFormBinder.forField(newPassword)
                .withValidator(new StringLengthValidator(errorMessage, 3, 255))
                .bind(ChangePasswordForm::getNewPassword, ChangePasswordForm::setNewPassword);

        changePasswordFormBinder.forField(repeatPassword)
                .withValidator(new StringLengthValidator(errorMessage, 3, 255))
                .withValidator(new AbstractValidator<>("Passwords do not match") {

                    @Override
                    public ValidationResult apply(String s, ValueContext valueContext) {
                        if (s.equals(newPassword.getValue())) {
                            return ValidationResult.ok();
                        }
                        return ValidationResult.error("Passwords do not match");
                    }
                })
                .bind(ChangePasswordForm::getRepeatPassword, ChangePasswordForm::setRepeatPassword);

        getFooter().add(new ButtonUtils("Change password", e -> {
            changePasswordFormBinder.validate();
            if (changePasswordFormBinder.isValid()) {
                ChangePasswordEvent.fireEvent(userProxyService, changePasswordFormBinder.getBean());
            }
        }).primaryLayout());
    }

    public Span generateLabel(String value) {
        return new SpanUtils().withText(value)
                .withStyle("width", "170px")
                .withStyle("display", "inline-block")
                .withStyle("text-align", "right")
                .withStyle("padding-right", "10px");
    }
}
