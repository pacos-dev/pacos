package org.pacos.core.component.registry.service;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pacos.base.exception.PacosException;
import org.pacos.core.component.registry.domain.Registry;
import org.pacos.core.component.registry.repository.RegistryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("coreTransactionManager")
public class RegistryService {

    private static final int MAX_KEY_LENGTH = 255;

    private final RegistryRepository registryRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public RegistryService(RegistryRepository registryRepository) {
        this.registryRepository = registryRepository;
        this.objectMapper = new ObjectMapper();
    }

    public void saveRegistry(RegistryName name, String value) {
        saveRegistry(name.name(), value);
    }

    public void saveRegistry(RegistryName name, Object object) {
        saveRegistry(name.name(), object);
    }

    /**
     * Saves a value under a namespaced key in the shared PacOS registry.
     * Existing enum-based registry entries use the same storage and transaction.
     */
    public void saveRegistry(String registryKey, String value) {
        registryRepository.save(new Registry(registryKey, value));
    }

    /**
     * Serializes and saves a value under a namespaced key in the shared PacOS registry.
     */
    public void saveRegistry(String registryKey, Object object) {
        try {
            registryRepository.save(new Registry(registryKey, mapToString(object)));
        } catch (JsonProcessingException e) {
            throw new PacosException(e);
        }
    }

    public <T> Optional<T> readRegistry(RegistryName name, Class<T> type) {
        return readRegistry(name.name(), type);
    }

    public <T> Optional<T> readRegistry(String registryKey, Class<T> type) {
        Optional<String> value = readRegistry(registryKey);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        try {
            if (type.equals(String.class)) {
                return Optional.of(type.cast(value.get()));
            } else if (type.equals(Integer.class)) {
                return Optional.of(type.cast(Integer.valueOf(value.get())));
            } else if (type.equals(Long.class)) {
                return Optional.of(type.cast(Long.valueOf(value.get())));
            } else if (type.equals(Double.class)) {
                return Optional.of(type.cast(Double.valueOf(value.get())));
            } else if (type.equals(Boolean.class)) {
                return Optional.of(type.cast(Boolean.valueOf(value.get())));
            } else {
                return Optional.of(readFromString(type, value.get()));
            }
        } catch (JsonProcessingException e) {
            throw new PacosException(e);
        }
    }

    public Optional<String> readRegistry(RegistryName registryName) {
        return readRegistry(registryName.name());
    }

    public Optional<String> readRegistry(String registryKey) {
        Registry registry = registryRepository.findByRegistryKey(requireValidRegistryKey(registryKey));
        if (registry == null) {
            return Optional.empty();
        }
        return Optional.of(registry.getValue());
    }

    public void delete(RegistryName registryName) {
        delete(registryName.name());
    }

    public void delete(String registryKey) {
        registryRepository.deleteByRegistryKey(requireValidRegistryKey(registryKey));
    }

    <T> T readFromString(Class<T> type, String value) throws JsonProcessingException {
        return objectMapper.readValue(value, type);
    }

    String mapToString(Object object) throws JsonProcessingException {
        if (object instanceof String string) {
            return string;
        }
        return objectMapper.writeValueAsString(object);
    }

    @Transactional(value = "coreTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void saveRegistryImmediately(RegistryName registryName, Object value) {
        saveRegistry(registryName, value);
    }

    @Transactional(value = "coreTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void saveRegistryImmediately(String registryKey, Object value) {
        saveRegistry(registryKey, value);
    }

    private static String requireValidRegistryKey(String registryKey) {
        if (registryKey == null || registryKey.isBlank()
                || !registryKey.equals(registryKey.strip())
                || registryKey.length() > MAX_KEY_LENGTH) {
            throw new IllegalArgumentException("Registry key must be non-blank, trimmed and at most 255 characters");
        }
        return registryKey;
    }
}
