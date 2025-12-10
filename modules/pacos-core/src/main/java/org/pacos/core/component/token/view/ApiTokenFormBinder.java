package org.pacos.core.component.token.view;

import java.time.LocalDate;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.AbstractValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.pacos.core.component.token.dto.ApiTokenForm;

public class ApiTokenFormBinder extends Binder<ApiTokenForm> {

    private final TextField tokenName;
    private final DatePicker expires;
    private final Checkbox neverExpires;

    ApiTokenFormBinder() {
        super(ApiTokenForm.class);
        this.tokenName = new TextField("Name");
        tokenName.setRequiredIndicatorVisible(true);
        tokenName.setHelperText("Provide unique tokenName");
        this.expires = new DatePicker("Expired on", LocalDate.now().plusDays(365));
        this.neverExpires = new Checkbox("Never");
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
