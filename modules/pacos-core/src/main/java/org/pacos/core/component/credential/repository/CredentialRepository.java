package org.pacos.core.component.credential.repository;

import java.util.List;

import org.pacos.base.credential.CredentialVisibility;
import org.pacos.core.component.credential.domain.CredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialRepository extends JpaRepository<CredentialEntity, String> {

    List<CredentialEntity> findAllByOwnerUserId(Integer ownerUserId);

    List<CredentialEntity> findAllByVisibility(CredentialVisibility visibility);
}
