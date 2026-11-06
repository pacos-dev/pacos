package org.pacos.base.credential;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

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
