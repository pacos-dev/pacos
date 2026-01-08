package org.pacos.core.component.token.view;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.AbstractValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.pacos.core.component.token.dto.ApiTokenForm;

import java.time.LocalDate;

public class ApiTokenFormBinder extends Binder<ApiTokenForm> {

    private static final String NAME="Name";
    private static final String NAME_HELP="Provide unique tokenName";
    private static final String EXPIRED="Expired on";
    private static final String NEVER="Never";

    private final TextField tokenName;
    private final DatePicker expires;
    private final Checkbox neverExpires;

    ApiTokenFormBinder() {
        super(ApiTokenForm.class);
        this.tokenName = new TextField(NAME);
        tokenName.setRequiredIndicatorVisible(true);
        tokenName.setHelperText(NAME_HELP);
        this.expires = new DatePicker(EXPIRED, LocalDate.now().plusDays(365));
        this.neverExpires = new Checkbox(NEVER);
        neverExpires.addValueChangeListener(event -> expires.setEnabled(!event.getValue()));
        neverExpires.getStyle().set("margin-top", "35px");

        forField(tokenName)
                .withValidator(new StringLengthValidator("Must have length in range [3-255]", 3, 255))
                .bind("tokenName");
        forField(expires)
                .withValidator(new AbstractValidator<>("Must be future date") {

                    @Override
                    public ValidationResult apply(LocalDate localDate, ValueContext valueContext) {
                        if (neverExpires.getValue()) {
                            return ValidationResult.ok();
                        }
                        if (localDate != null && localDate.isAfter(LocalDate.now())) {
                            return ValidationResult.ok();
                        } else {
                            return ValidationResult.error("Must be future date");
                        }
                    }
                }).bind("expires");
        forField(neverExpires).bind("neverExpires");
    }

    public static String getSearchIndex() {
        return NAME+NAME_HELP+EXPIRED+NEVER;
    }

    TextField getTokenName() {
        return tokenName;
    }

    DatePicker getExpires() {
        return expires;
    }

    Checkbox getNeverExpires() {
        return neverExpires;
    }
}
