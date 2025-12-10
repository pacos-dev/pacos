package org.pacos.base.utils.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TabUtilsTest {

    @Test
    void whenConstructorCalledThenTabHasLabel() {
        String label = "Tab 1";
        TabUtils tab = new TabUtils(label);

        assertTrue(tab.getChildren().findFirst().isPresent());
        assertEquals(label, ((Span)tab.getChildren().findFirst().get()).getText());
    }

    @Test
    void whenSetLabelTextCalledThenLabelTextIsUpdated() {
        String initialLabel = "Tab 1";
        String newLabel = "Updated Tab";

        TabUtils tab = new TabUtils(initialLabel);
        tab.setLabelText(newLabel);

        assertTrue(tab.getChildren().findFirst().isPresent());
        assertEquals(newLabel, ((Span)tab.getChildren().findFirst().get()).getText());
    }

    @Test
    void whenConstructorCalledWithComponentsThenComponentsAreAdded() {
        String label = "Tab 2";
        Component component = new Span("Component");

        TabUtils tab = new TabUtils(label, component);

        assertTrue(tab.getChildren().anyMatch(child -> child instanceof Span && ((Span) child).getText().equals("Component")));
    }
}
