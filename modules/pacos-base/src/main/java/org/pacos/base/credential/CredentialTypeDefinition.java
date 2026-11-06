package org.pacos.base.credential;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public record CredentialTypeDefinition(
        String key,
        String label,
        String description,
        List<CredentialFieldDefinition> fields) implements Serializable {

    public CredentialTypeDefinition {
        if (key == null || key.isBlank() || key.length() > 128) {
            throw new IllegalArgumentException("Credential type key must contain between 1 and 128 characters");
        }
        if (label == null || label.isBlank() || label.length() > 255) {
            throw new IllegalArgumentException("Credential type label must contain between 1 and 255 characters");
        }
        fields = List.copyOf(Objects.requireNonNull(fields, "Credential fields are required"));
        if (fields.stream().map(CredentialFieldDefinition::key).distinct().count() != fields.size()) {
            throw new IllegalArgumentException("Credential field keys must be unique");
        }
    }
}
