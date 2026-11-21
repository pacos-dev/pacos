package org.pacos.core.component.security.repository;

import java.util.Collection;
import java.util.Set;

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

    @Query("SELECT r.id FROM User u JOIN u.roles r WHERE u.id = :userId")
    Set<Integer> findRoleIdsByUserId(@Param("userId") int userId);
}