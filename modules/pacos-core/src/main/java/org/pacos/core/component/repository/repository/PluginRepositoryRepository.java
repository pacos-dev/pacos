package org.pacos.core.component.repository.repository;

import java.util.List;

import org.pacos.core.component.repository.domain.PluginRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PluginRepositoryRepository extends JpaRepository<PluginRepository, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByUrlIgnoreCase(String url);

    List<PluginRepository> findAllByOrderByNameAsc();
}
