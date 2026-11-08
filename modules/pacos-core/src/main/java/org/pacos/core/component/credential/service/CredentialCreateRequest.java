package org.pacos.core.component.credential.service;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import org.pacos.core.component.credential.service.CredentialVisibility;

/**
 * Core-only payload for creating a credential.
 *
 * <p>Values are validated against the registered credential type and encrypted before persistence.
 * File payloads are consumed and their internal buffers cleared by the core service.</p>
 */
public record CredentialCreateRequest(
        String name,
        String description,
        String type,
        CredentialVisibility visibility,
        Map<String, Object> values,
        Map<String, CredentialFileInput> files) implements Serializable {

    public CredentialCreateRequest {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Credential name is required");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Credential type is required");
        }
        Objects.requireNonNull(visibility, "Credential visibility is required");
        values = Map.copyOf(Objects.requireNonNull(values, "Credential values are required"));
        files = Map.copyOf(Objects.requireNonNull(files, "Credential files are required"));
    }
}
