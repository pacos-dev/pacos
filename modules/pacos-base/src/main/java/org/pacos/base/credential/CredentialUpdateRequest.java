package org.pacos.base.credential;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record CredentialUpdateRequest(
        String name,
        String description,
        CredentialVisibility visibility,
        Map<String, Object> values,
        Map<String, CredentialFileInput> files,
        Set<String> removeFields,
        Set<String> removeFiles) implements Serializable {

    public CredentialUpdateRequest {
        values = Map.copyOf(Objects.requireNonNull(values, "Credential values are required"));
        files = Map.copyOf(Objects.requireNonNull(files, "Credential files are required"));
        removeFields = Set.copyOf(Objects.requireNonNull(removeFields, "Fields to remove are required"));
        removeFiles = Set.copyOf(Objects.requireNonNull(removeFiles, "Files to remove are required"));
        if (name != null && name.isBlank()) {
            throw new IllegalArgumentException("Credential name cannot be blank");
        }
    }
}
