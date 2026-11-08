package org.pacos.base.credential;

import java.io.InputStream;
import java.util.Map;

/**
 * Closeable read-only view of decrypted credential values.
 *
 * <p>Callers must close this object as soon as the credential is no longer needed. Closing clears
 * internal secret buffers and closes file streams opened through this object. Returned secret arrays
 * are caller-owned copies and must be cleared by the caller.</p>
 */
public interface ResolvedCredential extends AutoCloseable {

    /**
     * Returns a non-secret text value, or {@code null} when the field is absent.
     * The returned {@link String} cannot be explicitly cleared.
     */
    String getText(String fieldKey);

    /**
     * Returns a copy of the secret value, or {@code null} when the field is absent.
     * The caller is responsible for clearing the returned array.
     */
    char[] getSecret(String fieldKey);

    /**
     * Returns an immutable copy of a key-value field, or an empty map when absent.
     */
    Map<String, String> getKeyValues(String fieldKey);

    /**
     * Opens a decrypted file stream. The stream is also closed when this object is closed.
     *
     * @return file stream, or {@code null} when the field is absent
     */
    InputStream openFile(String fieldKey);

    /**
     * Clears internal secret buffers and closes all file streams opened by this object.
     * This operation is idempotent.
     */
    @Override
    void close();
}
