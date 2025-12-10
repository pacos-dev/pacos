package org.pacos.common.view.menu;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IconFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuItemBuilderTest {

    private MenuItemBuilder builder;
    private IconFactory iconFactory;
    private Icon icon;

    @BeforeEach
    void setup() {
        builder = new MenuItemBuilder();
        iconFactory = mock(IconFactory.class);
        icon = spy(VaadinIcon.MENU.create());
        when(iconFactory.create()).thenReturn(icon);

    }

    @Test
    void whenBuildWithColorThenColorSetOnIconAndTextShadow() {
        Span item = builder.withColor(Color.RED).withIconFactory(iconFactory).withLabel("Test Label").build();

        verify(icon).setColor(Color.RED.getColor());
        Span labelSpan = (Span) item.getChildren().filter(c -> c instanceof Span).findFirst().orElse(null);
        assertNotNull(labelSpan);
        assertEquals("Test Label", labelSpan.getText());
        assertEquals("0px 0px 2px rgba(223, 12, 0,0.3)", labelSpan.getStyle().get("text-shadow"));
    }

    @Test
    void whenBuildWithIconFactoryThenIconAddedToSpan() {
        Span item = builder.withIconFactory(iconFactory).build();

        assertEquals(1,item.getChildren().count());
    }

    @Test
    void whenBuildWithLabelThenLabelSpanAdded() {
        Span item = builder.withLabel("Sample Label").build();

        Span labelSpan = (Span) item.getChildren().filter(c -> c instanceof Span).findFirst().orElse(null);
        assertNotNull(labelSpan);
        assertEquals("Sample Label", labelSpan.getText());
    }

    @Test
    void whenBuildWithShortcutThenShortcutSpanAdded() {
        Span item = builder.withShortcut("Ctrl+S").build();

        Span shortcutSpan = (Span) item.getChildren()
                .filter(c -> c instanceof Span && ((Span) c).getText().equals("[Ctrl+S]"))
                .findFirst()
                .orElse(null);
        assertNotNull(shortcutSpan);
        assertTrue(shortcutSpan.getClassNames().contains("short_cut"));
    }

    @Test
    void whenBuildWithoutOptionalFieldsThenOnlyClassSet() {
        Span item = builder.build();

        assertTrue(item.getClassNames().contains("menuitem"));
        assertFalse(item.getChildren().findFirst().isPresent());
    }
}
