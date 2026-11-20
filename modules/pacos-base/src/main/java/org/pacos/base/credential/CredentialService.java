package org.pacos.base.credential;

import java.util.List;

/**
 * Read-only credential access contract exposed to PacOS plugins.
 *
 * <p>The host binds this service to the calling plugin identity. Plugin code must not provide
 * an authorization context or select another plugin identity.</p>
 */
public interface CredentialService {

    /**
     * Finds credentials visible to the calling plugin.
     *
     * @param query optional type and text filters; {@code null} selects all visible credentials
     * @return metadata for credentials granted to this plugin
     * @throws CredentialAccessException if access is denied
     */
    List<CredentialDescriptor> findAvailable(CredentialQuery query);

    /**
     * Finds one visible credential without exposing its stored values.
     *
     * @param reference stable credential identifier
     * @return credential metadata
     * @throws CredentialAccessException if the credential does not exist or is not accessible
     */
    CredentialDescriptor findById(CredentialReference reference);

    /**
     * Resolves values and files granted to the calling plugin.
     *
     * @param reference stable credential identifier
     * @return closeable view of decrypted values; the caller must close it after use
     * @throws CredentialAccessException if the credential does not exist or is not accessible
     */
    ResolvedCredential resolve(CredentialReference reference);
}
