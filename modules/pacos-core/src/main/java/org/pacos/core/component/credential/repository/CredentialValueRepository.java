package org.pacos.core.component.credential.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.pacos.core.component.credential.domain.CredentialValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialValueRepository extends JpaRepository<CredentialValueEntity, String> {

    List<CredentialValueEntity> findAllByCredential_Id(String credentialId);

    List<CredentialValueEntity> findAllByCredential_IdIn(Collection<String> credentialIds);

    Optional<CredentialValueEntity> findByCredential_IdAndFieldKey(String credentialId, String fieldKey);

    void deleteByCredential_IdAndFieldKey(String credentialId, String fieldKey);

    void deleteAllByCredential_Id(String credentialId);
}
