package org.pacos.base.component.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ContentModeTest {

    @Test
    void whenContentModeCreatedThenCorrectDisplayName() {
        assertEquals("Text", ContentMode.TEXT.getDisplayName());
        assertEquals("Javascript", ContentMode.JAVASCRIPT.getDisplayName());
        assertEquals("JSON", ContentMode.JSON.getDisplayName());
        assertEquals("HTML", ContentMode.HTML.getDisplayName());
        assertEquals("XML", ContentMode.XML.getDisplayName());
        assertEquals("JAVA", ContentMode.JAVA.getDisplayName());
        assertEquals("SQL", ContentMode.SQL.getDisplayName());
    }

    @Test
    void whenContentModeEnumValuesThenAllModesArePresent() {
        ContentMode[] modes = ContentMode.values();

        assertArrayEquals(new ContentMode[]{
                ContentMode.TEXT, ContentMode.JAVASCRIPT, ContentMode.JSON,
                ContentMode.HTML, ContentMode.XML, ContentMode.JAVA, ContentMode.SQL
        }, modes);
    }

    @Test
    void whenContentModeEnumToStringThenCorrectName() {
        assertEquals("TEXT", ContentMode.TEXT.name());
        assertEquals("JAVASCRIPT", ContentMode.JAVASCRIPT.name());
        assertEquals("JSON", ContentMode.JSON.name());
        assertEquals("HTML", ContentMode.HTML.name());
        assertEquals("XML", ContentMode.XML.name());
        assertEquals("JAVA", ContentMode.JAVA.name());
        assertEquals("SQL", ContentMode.SQL.name());
    }
}
