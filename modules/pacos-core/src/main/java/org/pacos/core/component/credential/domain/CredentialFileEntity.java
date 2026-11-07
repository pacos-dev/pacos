package org.pacos.core.component.credential.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PACOS_CREDENTIAL_FILE",
        uniqueConstraints = @UniqueConstraint(name = "UK_CREDENTIAL_FILE_FIELD",
                columnNames = {"credential_id", "field_key"}))
@Getter
@Setter
public class CredentialFileEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credential_id", nullable = false)
    private CredentialEntity credential;

    @Column(name = "field_key", nullable = false, length = 128)
    private String fieldKey;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "content_type", length = 255)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long size;

    @Column(name = "checksum", nullable = false, length = 64)
    private String checksum;

    @Column(name = "storage_reference", nullable = false, unique = true, length = 36)
    private String storageReference;
}
