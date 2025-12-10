package org.pacos.common.view.grid;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;

public abstract class GridEditableNameExtension<T> implements ComponentEventListener<DetachEvent> {
    private final Editor<T> editor;

    protected GridEditableNameExtension(Grid<T> grid, Grid.Column<T> column) {
        this.editor = grid.getEditor();
        editor.setBuffered(true);

        Binder<T> binder = new Binder<>();
        editor.setBinder(binder);

        TextField nameField = new TextField();
        nameField.setWidthFull();
        binder.forField(nameField)
                .asRequired("Name can't be empty")
                .bind(getValue(), setValue());

        nameField.addKeyDownListener(Key.ESCAPE, e -> editor.cancel());
        nameField.addKeyDownListener(Key.ENTER, e -> changeNameEvent(editor));
        nameField.addKeyDownListener(Key.NUMPAD_ENTER, e -> changeNameEvent(editor));
        column.setEditorComponent(nameField);
        editor.addSaveListener(e -> {
            changeEvent(e.getItem());
        });
        grid.addDetachListener(this);

    }

    public abstract Setter<T, String> setValue();

    public abstract ValueProvider<T, String> getValue();

    public abstract void changeEvent(T bean);

    public void editItem(T e) {
        if (editor.isOpen()) {
            editor.cancel();
        }
        editor.editItem(e);
    }


    private void changeNameEvent(Editor<T> editor) {
        if (editor.getBinder().isValid()) {
            editor.save();
        }
    }

    public void close() {
        if (editor.isOpen()) {
            editor.cancel();
            editor.closeEditor();
        }
    }

    @Override
    public void onComponentEvent(DetachEvent event) {
        close();
    }
}
