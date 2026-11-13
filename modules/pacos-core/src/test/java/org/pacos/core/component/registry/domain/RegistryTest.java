package org.pacos.core.component.registry.domain;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.registry.service.RegistryName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistryTest {

    @Test
    void whenCreateRegistryWithEnumKeyThenStoreEnumName() {
        RegistryName registryName = RegistryName.SINGLE_MODE;
        String value = "Test value";

        Registry registry = new Registry(registryName, value);

        assertEquals(registryName.name(), registry.getRegistryKey());
        assertEquals(value, registry.getValue());
    }

    @Test
    void whenCreateRegistryWithNamespacedKeyThenStoreExactKey() {
        Registry registry = new Registry("plugin.apinity.global-execution-settings", "{}");

        assertEquals("plugin.apinity.global-execution-settings", registry.getRegistryKey());
        assertEquals("{}", registry.getValue());
    }

    @Test
    void whenCreateRegistryWithBlankKeyThenReject() {
        assertThrows(IllegalArgumentException.class, () -> new Registry(" ", "{}"));
    }

    @Test
    void whenCreateRegistryWithOversizedKeyThenReject() {
        assertThrows(IllegalArgumentException.class, () -> new Registry("x".repeat(256), "{}"));
    }

    @Test
    void whenSetValueThenValueIsUpdated() {
        Registry registry = new Registry();
        String value = "Updated value";

        registry.setValue(value);

        assertEquals(value, registry.getValue());
    }

    @Test
    void whenRegistryCreatedThenDefaultConstructorSetsFieldsToNull() {
        Registry registry = new Registry();

        assertNull(registry.getRegistryKey());
        assertNull(registry.getValue());
    }

    @Test
    void whenKeyIsTheSameThenEqualAndHashCodeMatch() {
        Registry registry = new Registry("plugin.example.settings", "true");
        Registry registry2 = new Registry("plugin.example.settings", "false");

        assertEquals(registry, registry2);
        assertEquals(registry.hashCode(), registry2.hashCode());
    }

    @Test
    void whenKeyIsDifferentThenEqualAndHashCodeNotMatch() {
        Registry registry = new Registry(RegistryName.INSTALLED, "true");
        Registry registry2 = new Registry(RegistryName.SINGLE_MODE, "true");

        assertNotEquals(registry, registry2);
        assertNotEquals(registry.hashCode(), registry2.hashCode());
    }
}
