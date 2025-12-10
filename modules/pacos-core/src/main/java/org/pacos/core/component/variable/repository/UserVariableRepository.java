package org.pacos.core.component.variable.repository;

import java.util.List;
import java.util.Optional;

import org.pacos.core.component.variable.domain.UserVariable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserVariableRepository extends JpaRepository<UserVariable, Integer> {

    void deleteAllByCollectionId(Integer collectionId);

    List<UserVariable> findAllByCollectionId(Integer variableCollectionId);

    Optional<UserVariable> findByCollectionIdAndName(Integer variableCollectionId, String name);

    @Query("select m from UserVariable m JOIN m.collection c where c.global = true and c.userId=:userId")
    List<UserVariable> findGlobalForUser(@Param("userId") Integer userId);

    @Query("select m from UserVariable m JOIN m.collection c " +
            "where c.global = true and c.userId=:userId and m.name = :name")
    Optional<UserVariable> loadGlobalVariablesByName(@Param("userId") Integer userId, @Param("name") String name);
}
