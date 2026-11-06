package org.pacos.core.component.credential.repository;

import java.util.List;
import java.util.Optional;

import org.pacos.base.credential.CredentialAclPermission;
import org.pacos.base.credential.CredentialPrincipalType;
import org.pacos.core.component.credential.domain.CredentialAccessEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialAccessRepository extends JpaRepository<CredentialAccessEntity, String> {

    List<CredentialAccessEntity> findAllByCredential_Id(String credentialId);

    Optional<CredentialAccessEntity> findByCredential_IdAndPrincipalTypeAndPrincipalIdAndPermission(
            String credentialId, CredentialPrincipalType principalType, String principalId,
            CredentialAclPermission permission);

    boolean existsByCredential_IdAndPrincipalTypeAndPrincipalIdAndPermission(
            String credentialId, CredentialPrincipalType principalType, String principalId,
            CredentialAclPermission permission);

    void deleteAllByCredential_Id(String credentialId);
}
