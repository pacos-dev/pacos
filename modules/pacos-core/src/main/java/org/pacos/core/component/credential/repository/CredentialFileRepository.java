package org.pacos.core.component.credential.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.pacos.core.component.credential.domain.CredentialFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialFileRepository extends JpaRepository<CredentialFileEntity, String> {

    List<CredentialFileEntity> findAllByCredential_Id(String credentialId);

    List<CredentialFileEntity> findAllByCredential_IdIn(Collection<String> credentialIds);

    Optional<CredentialFileEntity> findByCredential_IdAndFieldKey(String credentialId, String fieldKey);

    void deleteByCredential_IdAndFieldKey(String credentialId, String fieldKey);

    void deleteAllByCredential_Id(String credentialId);
}
