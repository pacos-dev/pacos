package org.pacos.core.component.dock.view.settings;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivatorConfigTest {

    @Test
    void whenActivatorConfigCreatedThenValuesAreAccessible() {
        List<String> activators = List.of("Activator1", "Activator2", "Activator3");
        ActivatorConfig config = new ActivatorConfig(activators);

        assertEquals(activators, config.activators());
        assertEquals(3, config.activators().size());
        assertTrue(config.activators().contains("Activator1"));
    }
}
