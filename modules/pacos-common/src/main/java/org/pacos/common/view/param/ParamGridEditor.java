package org.pacos.common.view.param;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.pacos.base.utils.notification.NotificationUtils;

/**
 * @deprecated
 */
@Deprecated
public class ParamGridEditor {

    private ParamGridEditor() {
    }

    public static void createEditor(ParamGrid grid, ParamGridColumnBuilder columns, Editor<Param> editor) {
        Binder<Param> binder = new Binder<>(Param.class);

        editor.setBinder(binder);
        editor.addCloseListener(e -> grid.editorCloseListener());

        if (columns.getNameColumn() != null) {
            TextField nameField = new TextField();
            nameField.setWidthFull();
            addCloseHandler(nameField, editor);
            binder.forField(nameField)
                    .withValidator(new StringLengthValidator("Can't be empty", 1, 255))
                    .bind(Param::getName, Param::setName);
            columns.getNameColumn().setEditorComponent(nameField);
            nameField.setClassName("last");
        }

        if (columns.getValueColumn() != null) {
            TextField valueField = new TextField();
            valueField.setWidthFull();
            addCloseHandler(valueField, editor);
            binder.forField(valueField)
                    .bind(Param::getValue, Param::setValue);
            columns.getValueColumn().setEditorComponent(valueField);
            valueField.setClassName("last");
        }

        grid.addItemDoubleClickListener(e -> {
            editor.editItem(e.getItem());
            Component editorComponent = e.getColumn().getEditorComponent();
            if (editorComponent instanceof Focusable) {
                ((Focusable<?>) editorComponent).focus();
            }
        });
    }

    public static void addCloseHandler(Component textField,
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
