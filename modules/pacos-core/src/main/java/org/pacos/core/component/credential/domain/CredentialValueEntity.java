package org.pacos.core.component.credential.domain;

import org.pacos.base.credential.CredentialFieldType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PACOS_CREDENTIAL_VALUE",
        uniqueConstraints = @UniqueConstraint(name = "UK_CREDENTIAL_VALUE_FIELD",
                columnNames = {"credential_id", "field_key"}))
@Getter
@Setter
public class CredentialValueEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credential_id", nullable = false)
    private CredentialEntity credential;

    @Column(name = "field_key", nullable = false, length = 128)
    private String fieldKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false, length = 32)
    private CredentialFieldType fieldType;

    @Lob
    @Column(name = "encrypted_value", nullable = false)
    private byte[] encryptedValue;
}
