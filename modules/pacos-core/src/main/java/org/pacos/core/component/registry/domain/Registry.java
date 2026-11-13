package org.pacos.core.component.registry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.pacos.core.component.registry.service.RegistryName;

/**
 * Stores one instance-wide configuration value in the PacOS registry.
 * Registry keys are strings so plugins can persist namespaced settings without extending the core enum.
 */
@Entity
@Table(name = "app_registry")
@Setter(AccessLevel.PACKAGE)
@Getter
@EqualsAndHashCode(of = "registryKey")
public class Registry {

    private static final int MAX_KEY_LENGTH = 255;

    @Id
    @Column(name = "registry_name", nullable = false, unique = true, length = MAX_KEY_LENGTH)
    private String registryKey;

    @Column(name = "value", length = 100000)
    private String value;

    public Registry() {
    }

    public Registry(RegistryName registryName, String value) {
        this(registryName.name(), value);
    }

    public Registry(String registryKey, String value) {
        this.registryKey = requireValidRegistryKey(registryKey);
        this.value = value;
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
