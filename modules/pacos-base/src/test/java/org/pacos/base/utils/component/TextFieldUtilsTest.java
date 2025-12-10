package org.pacos.base.utils.component;

import com.vaadin.flow.component.textfield.TextFieldVariant;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.Theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextFieldUtilsTest {

    @Test
    void whenConfigureSearchFieldCalledThenFieldIsConfiguredCorrectly() {
        String placeholder = "Search...";
        TextFieldUtils searchField = TextFieldUtils.configureSearchField(placeholder);

        assertTrue(searchField.hasThemeName(TextFieldVariant.LUMO_SMALL.getVariantName()));
        assertTrue(searchField.getClassNames().contains("search-filter"));
        assertTrue(searchField.isClearButtonVisible());
        assertEquals(placeholder, searchField.getPlaceholder());
        assertNotNull(searchField.getPrefixComponent());
    }

    @Test
    void whenWithPlaceholderCalledThenFieldHasPlaceholder() {
        String placeholder = "Enter text";
        TextFieldUtils field = TextFieldUtils.withPlaceholder(placeholder);

        assertEquals(placeholder, field.getPlaceholder());
        assertTrue(field.hasThemeName(TextFieldVariant.LUMO_SMALL.getVariantName()));
        assertTrue(field.hasThemeName(Theme.NO_BORDER.getName()));
    }

    @Test
    void whenConstructorCalledThenFieldIsInitializedEmpty() {
        TextFieldUtils field = new TextFieldUtils();

        assertNull(field.getLabel());
        assertFalse(field.isClearButtonVisible());
    }

    @Test
    void whenWithClassNameCalledThenFieldHasClassName() {
        String className = "custom-class";
        TextFieldUtils field = new TextFieldUtils().withClassName(className);

        assertTrue(field.getClassNames().contains(className));
    }

    @Test
    void whenWithWidthCalledThenFieldHasWidth() {
        int width = 200;
        TextFieldUtils field = new TextFieldUtils().withWidth(width);

        assertEquals("200.0px", field.getWidth());
    }

    @Test
    void whenWithFullWidthCalledThenFieldIsFullWidth() {
        TextFieldUtils field = new TextFieldUtils().withFullWidth();

        assertEquals("100%", field.getWidth());
    }

    @Test
    void whenWithLabelCalledThenFieldHasLabel() {
        String label = "Label";
        TextFieldUtils field = new TextFieldUtils("Label 0").withLabel(label);

        assertEquals(label, field.getLabel());
    }
}
