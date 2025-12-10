package org.pacos.core.component.variable.view.grid;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.AbstractValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.springframework.util.ObjectUtils;

public class VariableGridEditor {

    private VariableGridEditor() {
    }

    public static Binder<UserVariableDTO> createEditor(VariableGrid grid, VariableGridColumnConfigurator columns, Editor<UserVariableDTO> editor) {
        Binder<UserVariableDTO> binder = new Binder<>(UserVariableDTO.class);

        editor.setBinder(binder);
        editor.addCloseListener(e -> grid.editorCloseListener());

        if (columns.isNameColumnEditable()) {
            TextField nameField = new TextField();
            nameField.setWidthFull();
            addCloseHandler(nameField, editor);
            binder.forField(nameField)
                    .withValidator(getNameValidator(grid, editor))
                    .withValidator(new StringLengthValidator("Max length 255", 1, 255))
                    .asRequired()
                    .bind(UserVariableDTO::getName, UserVariableDTO::setName);
            columns.getNameColumn().setEditorComponent(nameField);
            nameField.setClassName("last");
        }

        if (columns.isInitValueColumnEditable()) {
            TextField valueField = new TextField();
            valueField.setWidthFull();
            addCloseHandler(valueField, editor);
            binder.forField(valueField)
                    .bind(UserVariableDTO::getInitialValue, UserVariableDTO::setInitialValue);
            columns.getInitValueColumn().setEditorComponent(valueField);
            valueField.setClassName("last");
        }

        if (columns.isCurrentValueColumnEditable()) {
            TextField valueField = new TextField();
            valueField.setWidthFull();
            addCloseHandler(valueField, editor);
            binder.forField(valueField)
                    .bind(UserVariableDTO::getCurrentValue, UserVariableDTO::setCurrentValue);
            columns.getCurrentValueColumn().setEditorComponent(valueField);
            valueField.setClassName("last");
        }

        grid.addItemDoubleClickListener(e -> {
            if (e.getItem() == null) {
                return;
            }
            editor.editItem(e.getItem());
            Component editorComponent = e.getColumn().getEditorComponent();
            if (editorComponent instanceof Focusable) {
                ((Focusable<?>) editorComponent).focus();
            }
        });

        return binder;
    }

    private static AbstractValidator<String> getNameValidator(VariableGrid grid, Editor<UserVariableDTO> lineEditor) {
        return new AbstractValidator<>("Variable with this name already exists") {
            @Override
            public ValidationResult apply(String name, ValueContext valueContext) {
                if (ObjectUtils.isEmpty(name)) {
                    return ValidationResult.error("Can't be empty");
                }
                if (lineEditor.getItem() != null && lineEditor.getItem().getName() != null
                        && lineEditor.getItem().getName().equals(name)) {
                    return ValidationResult.ok();
                }
                long count = grid.getVariables().stream().filter(e -> e.getName() != null && e.getName().equalsIgnoreCase(name)).count();
                if (count < 1) {
                    return ValidationResult.ok();
                }
                return ValidationResult.error("This name is already in use");
            }
        };
    }

    public static void addCloseHandler(Component textField,
                                       Editor<?> editor) {
        textField.getElement().addEventListener("keydown", e -> {
                    if (editor.isOpen() && editor.getBinder().isValid()) {
                        editor.save();
                        editor.closeEditor();
                    } else {
                        editor.cancel();
                        NotificationUtils.error("Variable name must be unique");
                    }
                })
                .setFilter("event.code === 'Escape' || event.code === 'Enter' || event.code === 'NumpadEnter'");
    }

}
