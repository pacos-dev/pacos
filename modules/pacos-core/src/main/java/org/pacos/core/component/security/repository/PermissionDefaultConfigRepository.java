package org.pacos.core.component.security.repository;

import java.util.List;

import org.pacos.core.component.security.domain.PermissionDefaultConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionDefaultConfigRepository extends JpaRepository<PermissionDefaultConfig, String> {

    @Query("SELECT c FROM PermissionDefaultConfig c ORDER BY c.action.category,c.action.key ASC ")
    List<PermissionDefaultConfig> findAllOrderByCategoryAndKey();

}