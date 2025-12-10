package org.pacos.core.component.registry.repository;

import org.pacos.core.component.registry.domain.Registry;
import org.pacos.core.component.registry.service.RegistryName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistryRepository extends JpaRepository<Registry, Integer> {

    Registry findByRegistryName(RegistryName registryName);

    void deleteByRegistryName(RegistryName registryName);
}
