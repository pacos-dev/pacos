package org.pacos.core.component.credential.service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * In-memory file payload used only by core credential creation and update operations.
 *
 * <p>The constructor and accessor defensively copy the content. Core must invoke {@link #clear()}
 * after processing the payload to zero the owned buffer.</p>
 */
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

    /**
     * Zeroes the internal file-content buffer after the input is no longer needed.
     */
    public void clear() {
        Arrays.fill(content, (byte) 0);
    }

    @Override
    public byte[] content() {
        return content.clone();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CredentialFileInput(thatOriginalFilename, thatContentType, byte[] thatContent))) {
            return false;
        }
        return Objects.equals(originalFilename, thatOriginalFilename)
                && Objects.equals(contentType, thatContentType)
                && Arrays.equals(content, thatContent);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(originalFilename, contentType) + Arrays.hashCode(content);
    }

    @Override
    public String toString() {
        return "CredentialFileInput[originalFilename=" + originalFilename
                + ", contentType=" + contentType + ", size=" + content.length + "]";
    }
}
