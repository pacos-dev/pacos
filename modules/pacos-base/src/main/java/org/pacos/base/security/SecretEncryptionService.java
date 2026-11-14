package org.pacos.base.security;

/**
 * Provides reversible encryption for sensitive values persisted by PacOS plugins.
 *
 * <p>The host supplies this service to plugin contexts. Implementations own key access and
 * cryptographic details; plugins must not manage or persist encryption keys themselves.</p>
 */
public interface SecretEncryptionService {

    /**
     * Encrypts a plaintext value for durable storage.
     *
     * @param plaintext value to encrypt; must not be {@code null}
     * @return versioned encrypted representation suitable for database storage
     */
    String encrypt(String plaintext);

    /**
     * Decrypts a value previously produced by {@link #encrypt(String)}.
     *
     * @param encryptedValue versioned encrypted representation
     * @return original plaintext
     */
    String decrypt(String encryptedValue);
}
