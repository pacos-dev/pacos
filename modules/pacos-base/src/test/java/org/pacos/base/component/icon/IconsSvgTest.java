package org.pacos.base.component.icon;

import com.vaadin.flow.component.icon.Icon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IconsSvgTest {

    @Test
    void whenFolderIconThenCreateReturnsCorrectIcon() {
        Icon icon = IconsSvg.FOLDER.create();

        assertEquals("my-icons-svg:folder", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenServerIconThenCreateReturnsCorrectIcon() {
        Icon icon = IconsSvg.SERVER.create();

        assertEquals("my-icons-svg:server", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenSqlFileIconThenCreateReturnsCorrectIcon() {
        Icon icon = IconsSvg.SQLFILE.create();

        assertEquals("my-icons-svg:sqlfile", icon.getElement().getAttribute("icon"));
    }
}