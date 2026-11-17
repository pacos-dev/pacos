
package org.pacos.core.component.variable.view.grid;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.dto.UserVariableDTO;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.TextField;

class VariableGridEditorTest {

    private TestVariableGrid grid;
    private UserVariableDTO variable;

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();
        grid = new TestVariableGrid();
        grid.configurator()
                .withNameColumn(true)
                .withInitValueColumn(true)
                .withCurrentValueColumn(true);
        grid.enableEditor(false);

        variable = new UserVariableDTO();
        variable.setName("existing");
        variable.setInitialValue("initial");
        variable.setCurrentValue("current");

        grid.updateVariableList(List.of(variable));
        UI.getCurrent().add(grid);

        grid.getEditor().editItem(variable);
        UI.getCurrent().getInternals()
                .getStateTree()
                .runExecutionsBeforeClientResponse();

        assertTrue(grid.getEditor().isOpen());
        assertSame(variable, grid.getEditor().getItem());
    }

    @Test
    void whenNameIsUnchangedThenEditorValidationSucceeds() {
        nameField().setValue("existing");

        assertTrue(grid.getEditor().getBinder().validate().isOk());
    }

    @Test
    void whenNameMatchesAnotherVariableIgnoringCaseThenEditorValidationFails() {
        UserVariableDTO another = new UserVariableDTO();
        another.setName("Another");
        grid.updateVariableList(List.of(variable, another));

        nameField().setValue("ANOTHER");

        assertFalse(grid.getEditor().getBinder().validate().isOk());
    }

    @Test
    void whenNameIsEmptyThenEditorValidationFails() {
        nameField().setValue("");

        assertFalse(grid.getEditor().getBinder().validate().isOk());
    }

    @Test
    void whenNameExceedsMaximumLengthThenEditorValidationFails() {
        nameField().setValue("x".repeat(256));

        assertFalse(grid.getEditor().getBinder().validate().isOk());
    }

    @Test
    void whenAllEditableFieldsContainValuesThenEditorValidationSucceeds() {
        nameField().setValue("updated");
        initValueField().setValue("new initial");
        currentValueField().setValue("new current");

        assertTrue(grid.getEditor().getBinder().validate().isOk());
    }

    private TextField nameField() {
        return (TextField) grid.configurator().getNameColumn().getEditorComponent();
    }

    private TextField initValueField() {
        return (TextField) grid.configurator().getInitValueColumn().getEditorComponent();
    }

    private TextField currentValueField() {
        return (TextField) grid.configurator().getCurrentValueColumn().getEditorComponent();
    }

    private static class TestVariableGrid extends VariableGrid {

        @Override
        public RemoveEvent onRemoveEvent() {
            return variable -> {
            };
        }

        @Override
        public SaveEvent onSaveEvent() {
            return variable -> {
            };
        }

        @Override
        public void onElemStateChange() {
        }
    }
}
