package org.pacos.core.component.registry.domain;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.registry.service.RegistryName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RegistryTest {

    @Test
    void whenCreateRegistryThenFieldsAreSet() {
        RegistryName registryName = RegistryName.SINGLE_MODE;
        String value = "Test value";

        Registry registry = new Registry(registryName, value);

        assertEquals(registryName, registry.getRegistryName());
        assertEquals(value, registry.getValue());
    }

    @Test
    void whenSetRegistryNameThenValueIsUpdated() {
        Registry registry = new Registry();
        RegistryName registryName = RegistryName.SINGLE_MODE;

        registry.setRegistryName(registryName);

        assertEquals(registryName, registry.getRegistryName());
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

        assertNull(registry.getRegistryName());
        assertNull(registry.getValue());
    }


    @Test
    void whenNameIsTheSameThenEqualAndHashCodeMatch() {
        Registry registry = new Registry(RegistryName.INSTALLED, "true");
        Registry registry2 = new Registry(RegistryName.INSTALLED, "false");

        assertEquals(registry, registry2);
        assertEquals(registry.hashCode(), registry2.hashCode());
    }

    @Test
    void whenNameIsDifferentThenEqualAndHashCodeNotMatch() {
        Registry registry = new Registry(RegistryName.INSTALLED, "true");
        Registry registry2 = new Registry(RegistryName.SINGLE_MODE, "true");

        assertNotEquals(registry, registry2);
        assertNotEquals(registry.hashCode(), registry2.hashCode());
    }

}
