package org.pacos.core.component.credential.service;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Full credential metadata used by PacOS core management screens and use cases.
 *
 * <p>This internal descriptor includes ownership, visibility, timestamps and manageability.
 * These administrative details are deliberately excluded from the plugin-facing descriptor.</p>
 */
public record CredentialAdminDescriptor(
        UUID id,
        String name,
        String description,
        String type,
        int ownerUserId,
        CredentialVisibility visibility,
        Instant createdAt,
        Instant updatedAt,
        Set<String> configuredFields,
        Set<String> configuredFiles,
        boolean manageable) implements Serializable {

    public CredentialAdminDescriptor {
        configuredFields = Set.copyOf(configuredFields);
        configuredFiles = Set.copyOf(configuredFiles);
    }
}
