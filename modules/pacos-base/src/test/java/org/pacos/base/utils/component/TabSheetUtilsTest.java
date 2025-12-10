package org.pacos.base.utils.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TabSheetUtilsTest {

    @Test
    void whenConstructorCalledThenTabSheetIsFullSize() {
        TabSheetUtils tabSheet = new TabSheetUtils();

        assertEquals(1, tabSheet.getChildren().count());
        assertEquals("100%", tabSheet.getWidth());
    }

    @Test
    void whenWithTabCalledThenTabAndContentAreAdded() {
        String label = "Tab 1";
        Component content = new Span("Content");

        TabSheetUtils tabSheet = new TabSheetUtils();
        tabSheet.withTab(label, content);

        assertEquals(2, tabSheet.getChildren().count()); // Ensure tab is added. Label is first component
    }
}
