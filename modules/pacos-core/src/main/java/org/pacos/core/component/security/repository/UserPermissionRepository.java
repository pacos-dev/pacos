package org.pacos.core.component.security.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.pacos.base.security.PermissionConfig;
import org.pacos.core.component.security.domain.UserPermission;
import org.pacos.core.component.security.dto.PermissionDetailConfig;
import org.pacos.core.component.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Integer> {

    @Query(value =
            "SELECT action.key,action.label,action.category, action.description,COALESCE(user.decision, def.decision) AS decision "
                    + "FROM APP_PERMISSION action LEFT JOIN APP_PERMISSION_USER user ON action.KEY = user.ACTION_KEY AND user.USER_ID = :userId "
                    + "LEFT JOIN APP_PERMISSION_DEF def on action.KEY = def.ACTION_KEY", nativeQuery = true)
    Set<PermissionDetailConfig> findUserPermissionsDetail(@Param("userId") int userId);

    @Query(value =
            "SELECT action.key, COALESCE(user.decision, def.decision) AS decision "
                    + "FROM APP_PERMISSION action LEFT JOIN APP_PERMISSION_USER user ON action.KEY = user.ACTION_KEY AND user.USER_ID = :userId "
                    + "LEFT JOIN APP_PERMISSION_DEF def on action.KEY = def.ACTION_KEY", nativeQuery = true)
    Set<PermissionConfig> findUserPermissions(@Param("userId") int userId);


    @Query(value = "SELECT a FROM UserPermission a WHERE a.user.id = :userId and a.action.key = :key")
    Optional<UserPermission> findByUserIdAndKey(@Param("userId") Integer userId, @Param("key") String key);

    List<UserPermission> user(User user);

    @Modifying
    @Query(value = "INSERT INTO APP_PERMISSION_USER (USER_ID,ACTION_KEY,DECISION) VALUES (:userId,:key,:accessDecision)",nativeQuery = true)
    void addNewPermission( @Param("key") String key,  @Param("userId") Integer userId, @Param("accessDecision") String accessDecision);
}
