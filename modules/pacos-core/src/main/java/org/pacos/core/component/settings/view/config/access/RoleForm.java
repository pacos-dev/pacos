package org.pacos.core.component.settings.view.config.access;

import org.pacos.base.utils.component.TextFieldUtils;
import org.pacos.core.component.security.dto.RoleDTO;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.StringLengthValidator;

public class RoleForm extends FormLayout {

    private final Binder<RoleDTO> binder;

    RoleForm(RoleDTO roleDTO) {
        this.binder = new Binder<>();
        binder.setBean(roleDTO == null ? new RoleDTO() : roleDTO);

        TextFieldUtils labelField = new TextFieldUtils("Name");
        labelField.setRequired(true);
        TextArea description = new TextArea("Description");
        add(labelField);
        add(description);

        binder.forField(labelField).asRequired()
                .withValidator(new StringLengthValidator("Length [3-255]", 3, 255))
                .bind(RoleDTO::getLabel, RoleDTO::setLabel);
        binder.forField(description).
                withValidator(new StringLengthValidator("Max length 2000-", 0, 255))
                .bind(RoleDTO::getDescription, RoleDTO::setDescription);
    }

    boolean validate() {
        binder.validate();
        return binder.isValid();
    }

    RoleDTO getBean() throws ValidationException {
        RoleDTO roleDTO = binder.getBean();
        binder.writeBean(roleDTO);
        return roleDTO;
    }

}
