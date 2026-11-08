package org.pacos.core.component.credential.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.pacos.core.component.credential.service.CredentialAclPermission;
import org.pacos.core.component.credential.service.CredentialPrincipalType;
import org.pacos.core.component.credential.domain.CredentialAccessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Provides persistence operations for credential access grants. */
public interface CredentialAccessRepository extends JpaRepository<CredentialAccessEntity, String> {

    List<CredentialAccessEntity> findAllByCredential_Id(String credentialId);

    List<CredentialAccessEntity> findAllByPrincipalTypeAndPrincipalIdAndPermissionIn(
            CredentialPrincipalType principalType, String principalId, Collection<CredentialAclPermission> permissions);

    List<CredentialAccessEntity> findAllByPrincipalTypeAndPrincipalIdInAndPermissionIn(
            CredentialPrincipalType principalType, Collection<String> principalIds,
            Collection<CredentialAclPermission> permissions);

    Optional<CredentialAccessEntity> findByCredential_IdAndPrincipalTypeAndPrincipalIdAndPermission(
            String credentialId, CredentialPrincipalType principalType, String principalId,
            CredentialAclPermission permission);

    @Query("""
            select case when count(credentialAccess) > 0 then true else false end
            from CredentialAccessEntity credentialAccess
            where credentialAccess.credential.id = :credentialId
              and credentialAccess.principalType = :principalType
              and credentialAccess.principalId in :principalIds
              and credentialAccess.permission in :permissions
            """)
    boolean hasAccessForPrincipals(
            @Param("credentialId") String credentialId,
            @Param("principalType") CredentialPrincipalType principalType,
            @Param("principalIds") Collection<String> principalIds,
            @Param("permissions") Collection<CredentialAclPermission> permissions);

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
