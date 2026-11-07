package org.pacos.core.component.credential.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.pacos.base.credential.CredentialAclPermission;
import org.pacos.base.credential.CredentialPrincipalType;
import org.pacos.core.component.credential.domain.CredentialAccessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Provides persistence operations for credential access grants. */
public interface CredentialAccessRepository extends JpaRepository<CredentialAccessEntity, String> {

    List<CredentialAccessEntity> findAllByCredential_Id(String credentialId);

    List<CredentialAccessEntity> findAllByPrincipalTypeAndPrincipalIdAndPermissionIn(
            CredentialPrincipalType principalType, String principalId, Collection<CredentialAclPermission> permissions);

    Optional<CredentialAccessEntity> findByCredential_IdAndPrincipalTypeAndPrincipalIdAndPermission(
            String credentialId, CredentialPrincipalType principalType, String principalId,
            CredentialAclPermission permission);

    /**
     * Checks whether the specified principal has the requested permission on a credential.
     *
     * @param credentialId credential identifier
     * @param principalType type of principal
     * @param principalId principal identifier
     * @param permission permission to check
     * @return whether the matching access grant exists
     */
    @Query("""
            select case when count(credentialAccess) > 0 then true else false end
            from CredentialAccessEntity credentialAccess
            where credentialAccess.credential.id = :credentialId
              and credentialAccess.principalType = :principalType
              and credentialAccess.principalId = :principalId
              and credentialAccess.permission = :permission
            """)
    boolean hasAccess(
            @Param("credentialId") String credentialId,
            @Param("principalType") CredentialPrincipalType principalType,
            @Param("principalId") String principalId,
            @Param("permission") CredentialAclPermission permission);

    void deleteAllByCredential_Id(String credentialId);
}
