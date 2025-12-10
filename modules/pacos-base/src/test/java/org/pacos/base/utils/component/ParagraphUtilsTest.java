package org.pacos.base.utils.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParagraphUtilsTest {

    @Test
    void whenConstructorWithTextCalledThenParagraphHasText() {
        String text = "Test Paragraph";
        ParagraphUtils paragraph = new ParagraphUtils(text);

        assertEquals(text, paragraph.getText());
    }

    @Test
    void whenWithStyleCalledThenStyleIsApplied() {
        String prop = "color";
        String val = "blue";
        ParagraphUtils paragraph = new ParagraphUtils("Test Paragraph");

        paragraph.withStyle(prop, val);

        assertEquals(val, paragraph.getStyle().get(prop));
    }

    @Test
    void whenWithMarginAutoCalledThenMarginIsAuto() {
        ParagraphUtils paragraph = new ParagraphUtils("Test Paragraph");

        paragraph.withMarginAuto();

        assertEquals("auto", paragraph.getStyle().get("margin"));
    }
}
