package org.pacos.core.component.security.repository;

import java.util.List;
import java.util.Set;

import org.pacos.core.component.security.domain.AppPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<AppPermission, Integer> {

    List<AppPermission> findAllByOrderByCategoryAscKeyAsc();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "INSERT INTO APP_ROLE_PERMISSION(ROLE_ID,PERMISSION_ID) VALUES (:roleId,:permissionId)", nativeQuery = true)
    void addPermissionForRole(@Param("permissionId") Integer permissionId, @Param("roleId") Integer roleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM APP_ROLE_PERMISSION WHERE ROLE_ID=:roleId AND PERMISSION_ID = :permissionId", nativeQuery = true)
    void removePermissionFromRole(@Param("permissionId") Integer permissionId, @Param("roleId") Integer roleId);

    @Query("SELECT p.key FROM User u LEFT JOIN u.roles r LEFT JOIN r.appPermissions p WHERE u.id = :userId")
    Set<String> findAllByUserId(@Param("userId") int userId);
}
