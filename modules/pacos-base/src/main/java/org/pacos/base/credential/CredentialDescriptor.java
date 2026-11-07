package org.pacos.base.credential;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record CredentialDescriptor(
        UUID id,
        String name,
        String description,
        String type,
        int ownerUserId,
        CredentialVisibility visibility,
        Instant createdAt,
        Instant updatedAt) implements Serializable {
}
