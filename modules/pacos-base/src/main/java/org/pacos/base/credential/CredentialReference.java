package org.pacos.base.credential;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record CredentialReference(UUID id) implements Serializable {

    public CredentialReference {
        Objects.requireNonNull(id, "Credential id is required");
    }

    public static CredentialReference of(String id) {
        return new CredentialReference(UUID.fromString(id));
    }
}
