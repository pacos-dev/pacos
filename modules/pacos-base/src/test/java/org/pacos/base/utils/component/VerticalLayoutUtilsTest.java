package org.pacos.base.utils.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class VerticalLayoutUtilsTest {

    @Test
    void whenDefaultsCalledThenLayoutIsConfiguredCorrectly() {
        Component component1 = new SpanUtils().withClassName("test");
        Component component2 = new SpanUtils().withWidthFull();
        VerticalLayoutUtils layout = VerticalLayoutUtils.defaults(component1, component2);

        assertEquals(2, layout.getComponentCount());
        assertFalse(layout.isPadding());
        assertFalse(layout.isSpacing());
        assertFalse(layout.isMargin());
    }

    @Test
    void whenMarginCalledThenLayoutHasMargin() {
        VerticalLayoutUtils layout = new VerticalLayoutUtils();
        layout.margin();

        assertTrue(layout.isMargin());
    }

    @Test
    void whenPaddingCalledThenLayoutHasPadding() {
        VerticalLayoutUtils layout = new VerticalLayoutUtils();
        layout.padding();

        assertTrue(layout.isPadding());
    }

    @Test
    void whenConfigureCalledThenLayoutIsConfiguredCorrectly() {
        VerticalLayout layout = mock(VerticalLayout.class);
        VerticalLayoutUtils.configure(layout);

        verify(layout).setSizeFull();
        verify(layout).setPadding(false);
        verify(layout).setSpacing(false);
        verify(layout).setMargin(false);
    }

    @Test
    void whenConstructorCalledThenLayoutContainsComponents() {
        Component component1 = new SpanUtils().withClassName("test");
        Component component2 = new SpanUtils().withWidthFull();
        VerticalLayoutUtils layout = new VerticalLayoutUtils(component1, component2);

        assertEquals(2, layout.getComponentCount());
    }
}
