package org.pacos.core.component.registry.repository;

import org.pacos.core.component.registry.domain.Registry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistryRepository extends JpaRepository<Registry, String> {

    Registry findByRegistryKey(String registryKey);

    void deleteByRegistryKey(String registryKey);
}
