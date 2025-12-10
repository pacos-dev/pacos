package org.pacos.core.component.installer.view.helper;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.installer.view.steps.helper.BoxContent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BoxContentTest {

    @Test
    void whenCreateBoxContentThenNoException() {
        assertDoesNotThrow(BoxContent::new);
    }

    @Test
    void whenAddTextLineThenNoException() {
        BoxContent boxContent = new BoxContent();
        assertDoesNotThrow(() -> boxContent.addTextLine("Test Line"));
    }
}
