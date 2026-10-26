package org.pacos.core.component.plugin.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CouplerPluginTest {

    private AppPlugin plugin;

    @BeforeEach
    void setUp() {
        plugin = new AppPlugin("com.example", "plugin-artifact", "1.0.0");
    }

    @Test
    void whenSetDisabledThenIsDisabledReturnsUpdatedValue() {
        plugin.setDisabled(true);
        assertTrue(plugin.isDisabled());

        plugin.setDisabled(false);
        assertFalse(plugin.isDisabled());
    }

    @Test
    void whenEqualObjectsThenEqualsReturnsTrue() {
        AppPlugin other = plugin;

        assertEquals(plugin, other);
    }

    @Test
    void whenDifferentObjectsThenEqualsReturnsFalse() {
        AppPlugin other = new AppPlugin("com.example", "plugin-artifact", "1.0.0");
        assertNotEquals(plugin, other);
    }

    @Test
    void whenNullObjectThenEqualsReturnsFalse() {
        assertNotEquals(null, plugin);
    }

    @Test
    void whenDifferentClassThenEqualsReturnsFalse() {
        Object differentClassObject = new Object();
        assertNotEquals(differentClassObject, plugin);
    }
}
