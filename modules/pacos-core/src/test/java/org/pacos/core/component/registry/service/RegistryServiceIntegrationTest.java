package org.pacos.core.component.registry.service;

import java.util.Arrays;
import java.util.Optional;

import org.config.IntegrationTestContext;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.registry.TestData;
import org.pacos.core.component.registry.repository.RegistryRepository;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistryServiceIntegrationTest extends IntegrationTestContext {

    @Autowired
    private RegistryService registryService;
    @Autowired
    private RegistryRepository registryRepository;

    @Test
    void whenSaveRegistryThenStoreInDb() {
        registryService.saveRegistry(RegistryName.DEFAULT_DOCK_CONFIG, "test");
        assertNotNull(registryRepository.findByRegistryKey(RegistryName.DEFAULT_DOCK_CONFIG.name()));
    }

    @Test
    void whenFindByNameThenReturnNullObject() {
        Optional<String> value = registryService.readRegistry(RegistryName.DEFAULT_DOCK_CONFIG);
        assertFalse(value.isPresent());
    }

    @Test
    void whenFindByNameThenReturnStoredObject() {
        registryService.saveRegistry(RegistryName.DEFAULT_DOCK_CONFIG, "test");
        Optional<String> value = registryService.readRegistry(RegistryName.DEFAULT_DOCK_CONFIG);
        assertTrue(value.isPresent());
        assertEquals("test", value.get());
    }

    @Test
    void whenSaveRegistryWithObjectThenStoreInDb() {
        TestData testData = new TestData();
        testData.setValues(Arrays.asList("test", "test2"));
        registryService.saveRegistry(RegistryName.DEFAULT_DOCK_CONFIG, testData);
        assertNotNull(registryRepository.findByRegistryKey(RegistryName.DEFAULT_DOCK_CONFIG.name()));
    }

    @Test
    void whenSaveRegistryWithObjectThenCanReadObject() {
        TestData testData = new TestData();
        testData.setValues(Arrays.asList("test", "test2"));
        registryService.saveRegistry(RegistryName.DEFAULT_DOCK_CONFIG, testData);
        Optional<TestData> data = registryService.readRegistry(RegistryName.DEFAULT_DOCK_CONFIG, TestData.class);
        assertTrue(data.isPresent());
        assertEquals(2, data.get().getValues().size());
        assertEquals("test", data.get().getValues().get(0));
        assertEquals("test2", data.get().getValues().get(1));
    }

    @Test
    void whenSaveAndReadNamespacedRegistryKeyThenPersistAndDeserialize() {
        String registryKey = "plugin.apinity.global-execution-settings";
        TestData settings = new TestData();
        settings.setValues(Arrays.asList("connect-timeout", "retry-enabled"));

        registryService.saveRegistry(registryKey, settings);

        Optional<TestData> restored = registryService.readRegistry(registryKey, TestData.class);
        assertTrue(restored.isPresent());
        assertEquals(settings.getValues(), restored.get().getValues());
        assertNotNull(registryRepository.findByRegistryKey(registryKey));
    }

    @Test
    void whenDeleteNamespacedRegistryKeyThenRemoveOnlySelectedEntry() {
        String registryKey = "plugin.apinity.global-execution-settings";
        registryService.saveRegistry(registryKey, "settings");
        registryService.saveRegistry(RegistryName.INSTALLED, "true");

        registryService.delete(registryKey);

        assertFalse(registryService.readRegistry(registryKey).isPresent());
        assertTrue(registryService.readRegistry(RegistryName.INSTALLED).isPresent());
    }
}
