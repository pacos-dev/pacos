package org.pacos.base.credential;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** Stable identifier used to address a credential without exposing its persistence entity. */
public record CredentialReference(UUID id) implements Serializable {

    public CredentialReference {
        Objects.requireNonNull(id, "Credential id is required");
    }

    public static CredentialReference of(String id) {
        return new CredentialReference(UUID.fromString(id));
    }
}
