package org.pacos.base.utils.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ListContentTest {

    @Test
    void whenInitializeListContentThenNoExceptions() {
        ListContent listContent = new ListContent();
        listContent.addRow("test", "test", true);
        listContent = new ListContent("test");
        listContent.addRow("test", "test", false);
        listContent.addRow("test", new SpanUtils("test"));
        //then
        assertNotNull(listContent);
    }

}