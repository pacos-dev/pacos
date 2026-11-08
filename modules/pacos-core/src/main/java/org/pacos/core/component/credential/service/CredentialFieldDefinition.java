package org.pacos.core.component.credential.service;

import java.io.Serializable;
import java.util.Objects;

/**
 * Defines one field accepted by a registered credential type.
 *
 * <p>{@code required} controls whether a value must exist. {@code sensitive} marks values that must
 * not be exposed by metadata-only reads; SECRET and FILE fields are always sensitive.
 * {@code multiple} is supported only for KEY_VALUE fields.</p>
 */
public record CredentialFieldDefinition(
        String key,
        String label,
        CredentialFieldType type,
        boolean required,
        boolean sensitive,
        boolean multiple) implements Serializable {

    public CredentialFieldDefinition {
        if (key == null || key.isBlank() || key.length() > 128) {
            throw new IllegalArgumentException("Field key must contain between 1 and 128 characters");
        }
        if (label == null || label.isBlank() || label.length() > 255) {
            throw new IllegalArgumentException("Field label must contain between 1 and 255 characters");
        }
        Objects.requireNonNull(type, "Field type is required");
        if (type == CredentialFieldType.SECRET && !sensitive) {
            throw new IllegalArgumentException("Secret fields must be sensitive");
        }
        if (sensitive && type != CredentialFieldType.SECRET && type != CredentialFieldType.FILE) {
            throw new IllegalArgumentException("Sensitive fields must use SECRET or FILE type");
        }
        if (type != CredentialFieldType.KEY_VALUE && multiple) {
            throw new IllegalArgumentException("Only key-value fields can be multiple");
        }
    }
}
