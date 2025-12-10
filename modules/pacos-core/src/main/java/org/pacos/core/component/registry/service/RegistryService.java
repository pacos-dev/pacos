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

    private final RegistryRepository registryRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public RegistryService(RegistryRepository registryRepository) {
        this.registryRepository = registryRepository;
        this.objectMapper = new ObjectMapper();
    }

    public void saveRegistry(RegistryName name, String value) {
        registryRepository.save(new Registry(name, value));
    }


    public void saveRegistry(RegistryName name, Object object) {
        try {
            registryRepository.save(new Registry(name, mapToString(object)));
        } catch (JsonProcessingException e) {
            throw new PacosException(e);
        }
    }

    public <T> Optional<T> readRegistry(RegistryName name, Class<T> t) {
        Optional<String> value = readRegistry(name);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        try {
            if (t.equals(String.class)) {
                return Optional.of((T) value.get());
            } else if (t.equals(Integer.class)) {
                return Optional.of((T) Integer.valueOf(value.get()));
            } else if (t.equals(Long.class)) {
                return Optional.of((T) Long.valueOf(value.get()));
            } else if (t.equals(Double.class)) {
                return Optional.of((T) Double.valueOf(value.get()));
            } else if (t.equals(Boolean.class)) {
                return Optional.of((T) Boolean.valueOf(value.get()));
            } else {
                return Optional.of(readFromString(t, value.get()));
            }
        } catch (JsonProcessingException e) {
            throw new PacosException(e);
        }
    }

    public Optional<String> readRegistry(RegistryName registryName) {
        final Registry registry = registryRepository.findByRegistryName(registryName);
        if (registry == null) {
            return Optional.empty();
        }
        return Optional.of(registry.getValue());
    }

    public void delete(RegistryName registryName) {
        registryRepository.deleteByRegistryName(registryName);
    }

    <T> T readFromString(Class<T> t, String value) throws JsonProcessingException {
        return objectMapper.readValue(value, t);
    }

    String mapToString(Object object) throws JsonProcessingException {
        if (object instanceof String) {
            return (String) object;
        }
        return objectMapper.writeValueAsString(object);
    }

    @Transactional(value = "coreTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void saveRegistryImmediately(RegistryName registryName, Object value) {
        saveRegistry(registryName, value);
    }
}
