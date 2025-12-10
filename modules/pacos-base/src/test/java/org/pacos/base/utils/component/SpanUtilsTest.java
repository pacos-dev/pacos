package org.pacos.base.utils.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpanUtilsTest {

    @Test
    void whenConstructorWithTextCalled_thenSpanHasText() {
        String text = "Test Text";
        SpanUtils span = new SpanUtils(text);

        assertEquals(text, span.getText());
    }

    @Test
    void whenConstructorWithoutTextCalled_thenSpanIsEmpty() {
        SpanUtils span = new SpanUtils();

        assertEquals("", span.getText());
    }

    @Test
    void whenOfClassCalled_thenSpanHasClassName() {
        String className = "test-class";
        SpanUtils span = SpanUtils.ofClass(className);

        assertTrue(span.getClassName().contains(className));
    }

    @Test
    void whenWithComponentsCalled_thenComponentsAreAdded() {
        SpanUtils span = new SpanUtils();
        Component component1 = new Span("Component 1");
        Component component2 = new Span("Component 2");

        span.withComponents(component1, component2);

        assertEquals(2, span.getChildren().count());
    }

    @Test
    void whenWithTextCalled_thenTextIsSet() {
        String text = "New Text";
        SpanUtils span = new SpanUtils();

        span.withText(text);

        assertEquals(text, span.getText());
    }

    @Test
    void whenWithStyleCalled_thenStyleIsApplied() {
        String prop = "color";
        String val = "red";
        SpanUtils span = new SpanUtils();

        span.withStyle(prop, val);

        assertEquals(val, span.getStyle().get(prop));
    }

    @Test
    void whenWithClassNameCalled_thenClassNameIsSet() {
        String className = "new-class";
        SpanUtils span = new SpanUtils();

        span.withClassName(className);

        assertTrue(span.getClassName().contains(className));
    }

    @Test
    void whenPaddingTopCalled_thenPaddingIsSet() {
        int padding = 10;
        SpanUtils span = new SpanUtils();

        span.paddingTop(padding);

        assertEquals(padding + "px", span.getStyle().get("padding-top"));
    }

    @Test
    void whenWithWidthFullCalled_thenWidthIsFull() {
        SpanUtils span = new SpanUtils();

        span.withWidthFull();

        assertEquals("100%", span.getWidth());
    }
}
