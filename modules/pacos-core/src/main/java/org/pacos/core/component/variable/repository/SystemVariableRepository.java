package org.pacos.core.component.variable.repository;

import org.pacos.core.component.variable.domain.SystemVariable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemVariableRepository extends JpaRepository<SystemVariable, Integer> {

    @Query("FROM SystemVariable v WHERE v.id != :id and v.name = :name")
    Optional<SystemVariable> isUniqe(@Param("id") Integer id, @Param("name") String name);

    Optional<SystemVariable> findByName(String variableName);

}
