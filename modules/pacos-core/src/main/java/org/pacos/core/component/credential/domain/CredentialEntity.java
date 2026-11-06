package org.pacos.core.component.credential.domain;

import java.time.Instant;

import org.pacos.base.credential.CredentialVisibility;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PACOS_CREDENTIAL")
@Getter
@Setter
public class CredentialEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "credential_type", nullable = false, length = 128)
    private String type;

    @Column(name = "owner_user_id", nullable = false)
    private Integer ownerUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 16)
    private CredentialVisibility visibility = CredentialVisibility.PRIVATE;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
