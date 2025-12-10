package org.pacos.core.component.registry.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.pacos.base.exception.PacosException;
import org.pacos.core.component.registry.domain.Registry;
import org.pacos.core.component.registry.repository.RegistryRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class RegistryServiceTest {

    @InjectMocks
    @Spy
    private RegistryService registryService;

    @Mock
    private RegistryRepository registryRepository;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenErrorDuringSerializationThenThrowException() throws JsonProcessingException {
        doThrow(JsonProcessingException.class).when(registryService).mapToString(any());
        //then
        assertThrows(PacosException.class, () -> registryService.saveRegistry(RegistryName.INSTALLED, Boolean.TRUE));
    }

    @Test
    void whenErrorDuringDeserializationThenThrowException() throws JsonProcessingException {
        doThrow(JsonProcessingException.class).when(registryService).readFromString(any(), any());
        Registry r = new Registry(RegistryName.INSTALLED, "true");
        when(registryRepository.findByRegistryName(RegistryName.INSTALLED)).thenReturn(r);
        //then
        assertThrows(PacosException.class, () -> registryService.readRegistry(RegistryName.INSTALLED, RegistryService.class));
    }
}