package org.pacos.base.utils.component;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiUtilsTest {

    @Test
    void whenWithComponentsCalledThenComponentsAreAdded() {
        LiUtils li = new LiUtils();
        HorizontalLayout layout = new HorizontalLayout();
        li.withComponents(layout);
        assertTrue(li.getChildren().anyMatch(component -> component.equals(layout)));
    }

    @Test
    void whenNoComponentsPassedThenNoComponentsAdded() {
        LiUtils li = new LiUtils();
        li.withComponents();
        assertFalse(li.getChildren().findAny().isPresent());
    }
}