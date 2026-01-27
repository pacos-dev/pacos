package org.pacos.core.component.security.repository;

import java.util.Collection;

import org.pacos.core.component.security.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

@Component
public interface RoleRepository extends JpaRepository<Role, Integer> {

    boolean existsByLabelAndIdNot(String label, Integer id);

    Collection<Role> findAllByOrderByLabelAsc();

    @Query(value = "SELECT count(*) FROM APP_ROLE_USER r WHERE r.ROLE_ID = :roleId", nativeQuery = true)
    int countRoleAssignment(@Param("roleId") int roleId);

    @Query(value = "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u LEFT JOIN u.roles r WHERE u.id=:userId AND r.id=:rootRole")
    boolean isUserHaveRootRole(@Param("userId") int userId, @Param("rootRole") Integer rootRole);
}
