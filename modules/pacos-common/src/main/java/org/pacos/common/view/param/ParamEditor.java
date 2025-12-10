package org.pacos.common.view.param;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.grid.editor.EditorCloseListener;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.AbstractValidator;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.pacos.base.utils.notification.NotificationUtils;

/**
 * Enabling param edition on dbl click
 */
public final class ParamEditor {

    private ParamEditor() {
    }


    static void createEditor(GridParam grid,
                             Grid.Column<Param> nameColumn,
                             Grid.Column<Param> valueColumn,
                             EditorCloseListener<Param> closeEvent) {

        Binder<Param> binder = new Binder<>(grid.getBeanType());
        grid.getEditor().setBinder(binder);

        TextField nameField = new TextField();
        nameField.setWidthFull();
        addCloseHandler(nameField, grid.getEditor());
        grid.getEditor().addCloseListener(closeEvent);

        binder.forField(nameField)
                .withValidator(new RegexpValidator("Whitespace is not allowed", "^\\S*$"))
                .withValidator(new StringLengthValidator("The value must be between 1 and 255 characters.", 1, 255))
                .withValidator(new AbstractValidator<>("This name is already in use") {
                    @Override
                    public ValidationResult apply(String value, ValueContext valueContext) {
                        boolean isUnique = grid.getItems().stream()
                                .filter(param -> !param.equals(grid.getEditor().getItem()))
                                .noneMatch(e -> e.getName().equals(value.trim()));
                        if (isUnique) {
                            return ValidationResult.ok();
                        }
                        return ValidationResult.error("This name is already in use");
                    }
                }).bind(Param::getName, Param::setName);
        nameColumn.setEditorComponent(nameField);
        nameField.setClassName("last");

        TextField valueField = new TextField();
        valueField.setWidthFull();
        addCloseHandler(valueField, grid.getEditor());
        binder.forField(valueField)
                .withValidator(new StringLengthValidator("The maximum length is 255", 0, 255))
                .bind(Param::getValue, Param::setValue);
        valueColumn.setEditorComponent(valueField);
        valueField.setClassName("last");


        grid.addItemDoubleClickListener(e -> onItemDoubleClickEvent(grid, e));
    }

    private static void onItemDoubleClickEvent(Grid<Param> grid, ItemDoubleClickEvent<Param> e) {
        grid.getEditor().editItem(e.getItem());
        Component editorComponent = e.getColumn().getEditorComponent();
        if (editorComponent instanceof Focusable) {
            ((Focusable<?>) editorComponent).focus();
        }
    }

    static void addCloseHandler(Component textField,
                                Editor<Param> editor) {
        textField.getElement().addEventListener("keydown", e -> {
                    if (editor.isOpen() && editor.getBinder().validate().isOk()) {
                        editor.cancel();
                    } else {
                        NotificationUtils.error("Can't save changes because of error in header form");
                    }
                })
                .setFilter("event.code === 'Escape' || event.code === 'Enter' || event.code === 'NumpadEnter'");
    }

}

