package org.pacos.base.utils.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DivUtilsTest {

    private DivUtils divUtils;

    @BeforeEach
    void setUp() {
        divUtils = new DivUtils();
    }

    @Test
    void whenOfClassCalledThenClassNamesAreAdded() {
        DivUtils div = DivUtils.ofClass("class1", "class2");
        assertTrue(div.getClassNames().contains("class1"));
        assertTrue(div.getClassNames().contains("class2"));
    }

    @Test
    void whenWithIdCalledThenIdIsSet() {
        divUtils.withId("id1");
        assertEquals("id1", divUtils.getId().orElse(""));
    }

    @Test
    void whenWithStyleCalledThenStyleIsApplied() {
        divUtils.withStyle("width", "100px");
        assertEquals("100px", divUtils.getStyle().get("width"));
    }

    @Test
    void whenWithTextCalledThenTextIsSet() {
        divUtils.withText("Test text");
        assertEquals("Test text", divUtils.getText());
    }

    @Test
    void whenWithComponentsCalledThenComponentsAreAdded() {
        Component component = new Span();
        divUtils.withComponents(component);
        assertTrue(divUtils.getChildren().anyMatch(c -> c.equals(component)));
    }

    @Test
    void whenWithMarginAutoCalledThenMarginIsAuto() {
        divUtils.withMarginAuto();
        assertEquals("auto", divUtils.getStyle().get("margin"));
    }

    @Test
    void whenWithClassNameCalledThenClassNameIsAdded() {
        divUtils.withClassName("class1");
        assertTrue(divUtils.getClassNames().contains("class1"));
    }
}

