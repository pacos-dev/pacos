package org.pacos.base.utils.component;

import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LabelUtilsTest {

    @Test
    void whenWithStyleCalledThenStyleIsApplied() {
        LabelUtils label = new LabelUtils("Test");
        label.withStyle("color", "red");
        assertEquals("red", label.getStyle().get("color"));
    }

    @Test
    void whenTetiaryCalledThenClassIsSetToTetiary() {
        Span label = LabelUtils.tetiary("Test");
        assertTrue(label.getClassName().contains("tetiary"));
    }

    @Test
    void whenNoSelectableCalledThenClassIsSetToNoSelect() {
        LabelUtils label = LabelUtils.noSelectable("Test");
        assertTrue(label.getClassName().contains("no-select"));
    }

    @Test
    void whenWithClassNameCalledThenClassIsAdded() {
        LabelUtils label = new LabelUtils("Test");
        label.withClassName("custom-class");
        assertTrue(label.getClassName().contains("custom-class"));
    }
}
