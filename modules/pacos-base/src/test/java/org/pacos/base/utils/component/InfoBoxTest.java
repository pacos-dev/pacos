package org.pacos.base.utils.component;

import com.vaadin.flow.component.Text;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfoBoxTest {

    @Test
    void whenInfoBoxIsCreatedThenItContainsCorrectClassesAndComponents() {
        String info = "Test Info";
        InfoBox infoBox = new InfoBox(info);

        assertNotNull(infoBox);
        assertTrue(infoBox.getClassName().contains("alert"));
        assertTrue(infoBox.getClassName().contains("info"));
    }

    @Test
    void whenInfoBoxIsCreatedWithComponentsThenItContainsCorrectClassesAndComponents() {
        String info = "Test Info";
        InfoBox infoBox = new InfoBox(new Text(info));

        assertNotNull(infoBox);
        assertTrue(infoBox.getClassName().contains("alert"));
        assertTrue(infoBox.getClassName().contains("info"));
    }
}
