package org.pacos.core.component.variable.view.grid;

import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.dto.UserVariableDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class VariableGridEditorTest {

    @Test
    void whenNameIsNewThenNameValidatorAcceptsIt() {
        TestVariableGrid grid = new TestVariableGrid();
        grid.configurator().withNameColumn(true);
        Editor<UserVariableDTO> editor = grid.getEditor();
        VariableGridEditor.createEditor(grid, grid.configurator(), editor);
        UserVariableDTO item = new UserVariableDTO();
        item.setName("existing");
        grid.setVariables(java.util.List.of(item));

        assertTrue(editor.getBinder().validate().isOk());
    }

    @Test
    void whenNameIsEmptyThenNameValidatorRejectsIt() {
        TestVariableGrid grid = new TestVariableGrid();
        grid.configurator().withNameColumn(true);
        Editor<UserVariableDTO> editor = grid.getEditor();
        VariableGridEditor.createEditor(grid, grid.configurator(), editor);
        UserVariableDTO item = new UserVariableDTO();
        item.setName("");
        grid.getEditor().editItem(item);

        TextField field = (TextField) grid.configurator().getNameColumn().getEditorComponent();
        field.setValue("");

        assertFalse(editor.getBinder().isValid());
    }

    @Test
    void whenNameIsUnchangedThenNameValidatorAcceptsIt() {
        TestVariableGrid grid = new TestVariableGrid();
        grid.configurator().withNameColumn(true);
        Editor<UserVariableDTO> editor = grid.getEditor();
        VariableGridEditor.createEditor(grid, grid.configurator(), editor);
        UserVariableDTO item = new UserVariableDTO();
        item.setName("same");
        grid.setVariables(java.util.List.of(item));
        editor.editItem(item);

        TextField field = (TextField) grid.configurator().getNameColumn().getEditorComponent();
        field.setValue("same");

        assertTrue(editor.getBinder().isValid());
        assertEquals("same", item.getName());
    }

    private static final class TestVariableGrid extends VariableGrid {
        @Override
        public RemoveEvent onRemoveEvent() {
            return item -> {
            };
        }

        @Override
        public SaveEvent onSaveEvent() {
            return item -> {
            };
        }

        @Override
        public void onElemStateChange() {
        }
    }
}
