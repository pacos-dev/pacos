package org.pacos.core.component.installer.view.helper;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.installer.view.steps.helper.BoxMode;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BoxModeTest {

    @Test
    void whenCreateBoxModeThenNoException() {
        assertDoesNotThrow(BoxMode::new);
    }

    @Test
    void whenSetImageThenNoException() {
        BoxMode boxMode = new BoxMode();
        assertDoesNotThrow(() -> boxMode.setImage("test-image.png"));
    }

    @Test
    void whenAddListItemThenNoException() {
        BoxMode boxMode = new BoxMode();
        assertDoesNotThrow(() -> boxMode.addListItem("Test Item"));
    }

    @Test
    void whenAddTitleThenNoException() {
        BoxMode boxMode = new BoxMode();
        assertDoesNotThrow(() -> boxMode.addTitle("Test Title"));
    }
}
