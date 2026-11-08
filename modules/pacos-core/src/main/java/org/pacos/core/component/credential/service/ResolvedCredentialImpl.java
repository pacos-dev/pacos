package org.pacos.core.component.credential.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.pacos.base.credential.ResolvedCredential;

/**
 * Owns decrypted credential buffers and file streams for one resolution operation.
 *
 * <p>Closing the instance is idempotent and zeroes all retained mutable secret material.</p>
 */
class ResolvedCredentialImpl implements ResolvedCredential {

    private final Map<String, Object> values;
    private final Map<String, FileResolver> files;
    private final Set<InputStream> openedStreams = new HashSet<>();
    private boolean closed;

    ResolvedCredentialImpl(Map<String, Object> values, Map<String, FileResolver> files) {
        this.values = new HashMap<>(values);
        this.files = new HashMap<>(files);
    }

    @Override
    public String getText(String fieldKey) {
        ensureOpen();
        Object value = values.get(fieldKey);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String text)) {
            throw new IllegalArgumentException("Credential field is not text: " + fieldKey);
        }
        return text;
    }

    @Override
    public char[] getSecret(String fieldKey) {
        ensureOpen();
        Object value = values.get(fieldKey);
        if (value == null) {
            return null;
        }
        if (!(value instanceof char[] secret)) {
            throw new IllegalArgumentException("Credential field is not a secret: " + fieldKey);
        }
        return secret.clone();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> getKeyValues(String fieldKey) {
        ensureOpen();
        Object value = values.get(fieldKey);
        if (value == null) {
            return Map.of();
        }
        if (!(value instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("Credential field is not a key-value collection: " + fieldKey);
        }
        return Map.copyOf((Map<String, String>) value);
    }

    @Override
    public InputStream openFile(String fieldKey) {
        ensureOpen();
        FileResolver resolver = files.get(fieldKey);
        if (resolver == null) {
            return null;
        }
        InputStream stream = new SensitiveInputStream(resolver.load());
        openedStreams.add(stream);
        return stream;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        values.values().stream()
                .filter(char[].class::isInstance)
                .map(char[].class::cast)
                .forEach(secret -> java.util.Arrays.fill(secret, '\0'));
        openedStreams.forEach(stream -> {
            try {
                stream.close();
            } catch (java.io.IOException ignored) {
            }
        });
        openedStreams.clear();
        values.clear();
        files.clear();
        closed = true;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Resolved credential is already closed");
        }
    }

    @FunctionalInterface
    interface FileResolver {
        byte[] load();
    }

    private static final class SensitiveInputStream extends ByteArrayInputStream {

        private SensitiveInputStream(byte[] buffer) {
            super(buffer);
        }

        @Override
        public void close() {
            java.util.Arrays.fill(buf, (byte) 0);
            pos = count;
        }
    }
}
