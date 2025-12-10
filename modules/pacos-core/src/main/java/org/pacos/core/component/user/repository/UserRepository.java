package org.pacos.core.component.user.repository;

import java.util.Optional;

import org.pacos.core.component.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByLogin(String userName);

    Optional<User> findByLogin(String login);

    Optional<User> findByToken(String tokenValue);

    @Modifying
    @Query("UPDATE User set token=:token where id = :id")
    void saveToken(@Param("token") String token, @Param("id") int id);

    @Modifying
    @Query("UPDATE User set variableCollectionId=:collectionId where id = :userId")
    void saveVariableCollection(@Param("userId") int id, @Param("collectionId") Integer variableCollectionId);
}
