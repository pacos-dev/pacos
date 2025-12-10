package org.pacos.core.component.token.view;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.pacos.base.api.AccessToken;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.token.ApiPermissions;
import org.pacos.core.component.token.dto.ApiTokenForm;
import org.pacos.core.component.token.service.ApiTokenService;

public class ApiTokenFormLayout extends VerticalLayout {

    private final ApiTokenFormBinder formBinder;
    private final ApiTokenService tokenService;
    private final ApiTokenGridView tokenListView;
    private final TextField tokenField;

    public ApiTokenFormLayout(ApiTokenService tokenService, ApiTokenGridView tokenListView) {
        this.tokenService = tokenService;
        this.tokenListView = tokenListView;
        this.formBinder = new ApiTokenFormBinder();

        HorizontalLayout newTokenForm =
                new HorizontalLayout(formBinder.getTokenName(), formBinder.getExpires(), formBinder.getNeverExpires());

        this.tokenField = new TextField("Token");
        this.tokenField.setVisible(false);
        this.tokenField.setWidthFull();
        this.tokenField.setHelperText("Generated access token. Save it because you won't be able to view it later");

        add(newTokenForm);
        add(tokenField);
        add(new ButtonUtils("Generate token", e -> saveTokenEvent())
                .withEnabledForPermission(ApiPermissions.GENERATE_TOKEN));
    }

    void saveTokenEvent() {
        if (!formBinder.validate().hasErrors()) {
            try {
                ApiTokenForm tokenForm = formBinder.writeRecord();
                AccessToken accessToken = tokenService.createToken(tokenForm);
                this.tokenField.setVisible(true);
                this.tokenField.setValue(accessToken.token());
                tokenListView.refresh();
            } catch (Exception e) {
                NotificationUtils.error(e.getMessage());
            }
        }
    }

    ApiTokenFormBinder getFormBinder() {
        return formBinder;
    }
}
