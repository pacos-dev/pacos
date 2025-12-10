package org.pacos.core.component.registry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.pacos.core.component.registry.service.RegistryName;

@Entity
@Table(name = "app_registry")
@Setter(AccessLevel.PACKAGE)
@Getter
@EqualsAndHashCode(of = "registryName")
public class Registry {

    @Id
    @Column(name = "registry_name", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private RegistryName registryName;

    @Column(name = "value", length = 100000)
    private String value;

    public Registry() {
    }

    public Registry(RegistryName registryName, String value) {
        this.registryName = registryName;
        this.value = value;
    }

}
