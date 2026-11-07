package org.pacos.base.credential;

import java.io.Serializable;
import java.util.Objects;

public record CredentialFileInput(String originalFilename, String contentType, byte[] content) implements Serializable {

    public CredentialFileInput {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Original filename is required");
        }
        Objects.requireNonNull(content, "File content is required");
        content = content.clone();
    }

    public int size() {
        return content.length;
    }

    @Override
    public byte[] content() {
        return content.clone();
    }
}
