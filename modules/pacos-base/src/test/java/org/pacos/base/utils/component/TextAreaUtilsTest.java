package org.pacos.base.utils.component;

import com.vaadin.flow.component.textfield.TextAreaVariant;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.Theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextAreaUtilsTest {

    @Test
    void whenConstructorCalledThenTextAreaIsConfiguredCorrectly() {
        TextAreaUtils textArea = new TextAreaUtils();

        assertTrue(textArea.getThemeNames().contains(TextAreaVariant.LUMO_SMALL.getVariantName()));
        assertTrue(textArea.getThemeNames().contains(Theme.NO_BORDER.getName()));
        assertEquals("100%", textArea.getWidth());
        assertNull(textArea.getLabel());
    }

    @Test
    void whenConstructorWithLabelCalledThenTextAreaHasLabel() {
        String label = "Description";
        TextAreaUtils textArea = new TextAreaUtils(label);

        assertTrue(textArea.getThemeNames().contains(TextAreaVariant.LUMO_SMALL.getVariantName()));
        assertTrue(textArea.getThemeNames().contains(Theme.NO_BORDER.getName()));
        assertEquals("100%", textArea.getWidth());
        assertEquals(label, textArea.getLabel());
    }
}
