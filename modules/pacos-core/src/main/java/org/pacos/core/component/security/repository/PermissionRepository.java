package org.pacos.core.component.security.repository;

import java.util.Optional;

import org.pacos.core.component.security.domain.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permissions, String> {

    Optional<Permissions> findByKey(String key);
}
