package org.pacos.base.credential;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

/**
 * Non-secret credential metadata exposed to plugins.
 *
 * <p>It contains only the identifier and field metadata needed to select and consume a credential.
 * Ownership, visibility, timestamps and management capabilities remain internal to PacOS core.</p>
 */
public record CredentialDescriptor(
        UUID id,
        String name,
        String description,
        String type,
        Set<String> configuredFields,
        Set<String> configuredFiles) implements Serializable {

    public CredentialDescriptor {
        configuredFields = Set.copyOf(configuredFields);
        configuredFiles = Set.copyOf(configuredFiles);
    }
}
