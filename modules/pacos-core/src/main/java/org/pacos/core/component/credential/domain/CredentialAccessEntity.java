package org.pacos.core.component.credential.domain;

import org.pacos.core.component.credential.service.CredentialAclPermission;
import org.pacos.core.component.credential.service.CredentialPrincipalType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * Persists one group or plugin grant for a credential.
 *
 * <p>Plugin principals are restricted to {@code ACCESS}; group grants may use {@code ACCESS}
 * or {@code MANAGE}. The service enforces the guest-group restriction.</p>
 */
@Entity
@Table(name = "PACOS_CREDENTIAL_ACCESS",
        uniqueConstraints = @UniqueConstraint(name = "UK_CREDENTIAL_ACCESS",
                columnNames = {"credential_id", "principal_type", "principal_id", "permission"}))
@Getter
@Setter
public class CredentialAccessEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credential_id", nullable = false)
    private CredentialEntity credential;

    @Enumerated(EnumType.STRING)
    @Column(name = "principal_type", nullable = false, length = 16)
    private CredentialPrincipalType principalType;

    @Column(name = "principal_id", nullable = false, length = 255)
    private String principalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 16)
    private CredentialAclPermission permission;
}
