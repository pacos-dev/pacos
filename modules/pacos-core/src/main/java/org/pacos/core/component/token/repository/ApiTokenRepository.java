package org.pacos.core.component.token.repository;

import java.time.LocalDate;
import java.util.List;

import org.pacos.core.component.token.domain.ApiToken;
import org.pacos.core.component.token.domain.ApiTokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiTokenRepository extends JpaRepository<ApiToken, String> {

    List<ApiToken> findAllByOrderByNameAsc();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ApiToken SET status=:status WHERE name=:name")
    void updateStatus(@Param("name") String name, @Param("status") ApiTokenStatus apiTokenStatus);

    @Query("""
            SELECT count(a) > 0 FROM ApiToken a
            WHERE a.token = :token
             AND (a.expiredOn > :currentDate OR a.expiredOn IS NULL)
             AND a.status=:status
            """)
    boolean isActiveToken(@Param("token") String authToken, @Param("currentDate") LocalDate now,
            @Param("status") ApiTokenStatus apiTokenStatus);

    @Modifying(clearAutomatically = true)
    @Query("""
                    UPDATE ApiToken a SET a.status=:inactive 
                    WHERE (a.expiredOn IS NOT NULL AND a.expiredOn<=:currentDate)
                                AND a.status=:active
            """)
    void updateTokenAsExpiredIfExpirationDayReached(@Param("currentDate") LocalDate now,
            @Param("active") ApiTokenStatus activeStatus,
            @Param("inactive") ApiTokenStatus inactiveStatus);
}
