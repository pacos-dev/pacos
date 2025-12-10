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
        //when
        registryService.saveRegistry(RegistryName.DEFAULT_DOCK_CONFIG, "test");
        //then
        assertNotNull(registryRepository.findByRegistryName(RegistryName.DEFAULT_DOCK_CONFIG));
    }

    @Test
    void whenFindByNameThenReturnNullObject() {
        //when
        Optional<String> value = registryService.readRegistry(RegistryName.DEFAULT_DOCK_CONFIG);
        //then
        assertFalse(value.isPresent());
    }

    @Test
    void whenFindByNameThenReturnStoredObject() {
        //given
        registryService.saveRegistry(RegistryName.DEFAULT_DOCK_CONFIG, "test");
        //when
        Optional<String> value = registryService.readRegistry(RegistryName.DEFAULT_DOCK_CONFIG);
        //then
        assertTrue(value.isPresent());
        assertEquals("test", value.get());
    }

    @Test
    void whenSaveRegistryWithObjectThenStoreInDb() {
        //given
        TestData testData = new TestData();
        testData.setValues(Arrays.asList("test", "test2"));
        //when
        registryService.saveRegistry(RegistryName.DEFAULT_DOCK_CONFIG, testData);
        //then
        assertNotNull(registryRepository.findByRegistryName(RegistryName.DEFAULT_DOCK_CONFIG));
    }

    @Test
    void whenSaveRegistryWithObjectThenCanReadObject() {
        //given
        TestData testData = new TestData();
        testData.setValues(Arrays.asList("test", "test2"));
        //when
        registryService.saveRegistry(RegistryName.DEFAULT_DOCK_CONFIG, testData);
        //then
        Optional<TestData> data = registryService.readRegistry(RegistryName.DEFAULT_DOCK_CONFIG, TestData.class);
        assertTrue(data.isPresent());
        assertEquals(2, data.get().getValues().size());
        assertEquals("test", data.get().getValues().get(0));
        assertEquals("test2", data.get().getValues().get(1));
    }

}