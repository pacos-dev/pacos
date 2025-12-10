package org.pacos.core.component.variable.repository;

import java.util.List;

import org.pacos.core.component.variable.domain.UserVariableCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserVariableCollectionRepository extends JpaRepository<UserVariableCollection, Integer> {

    @Query("SELECT e FROM UserVariableCollection e WHERE e.userId=:userId ORDER BY LOWER(e.name) ASC")
    List<UserVariableCollection> findByUserIdOrderByName(@Param("userId") Integer userId);
}
