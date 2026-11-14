package org.pacos.core.component.credential.security;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import org.pacos.base.security.SecretEncryptionService;
import org.springframework.stereotype.Component;

/**
 * Plugin-facing encryption service backed by the PacOS Vault cryptography.
 *
 * <p>Associated data is deliberately independent of record identifiers so encrypted values can
 * be copied when a plugin entity is cloned. The format prefix allows the representation to evolve
 * without coupling plugin persistence to the Vault's internal byte format.</p>
 */
@Component
public class PluginSecretEncryptionService implements SecretEncryptionService {

    private static final String FORMAT_PREFIX = "v1:";
    private static final byte[] ASSOCIATED_DATA = "pacos:plugin-secret:v1".getBytes(StandardCharsets.UTF_8);

    private final VaultCryptography cryptography;

    /**
     * Creates the plugin secret encryption service.
     *
     * @param cryptography PacOS Vault authenticated encryption service
     */
    public PluginSecretEncryptionService(VaultCryptography cryptography) {
        this.cryptography = cryptography;
    }

    @Override
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException("Plaintext is required");
        }
        byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        try {
            byte[] encryptedBytes = cryptography.encrypt(plaintextBytes, ASSOCIATED_DATA);
            try {
                return FORMAT_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes);
            } finally {
                Arrays.fill(encryptedBytes, (byte) 0);
            }
        } finally {
            Arrays.fill(plaintextBytes, (byte) 0);
        }
    }

    @Override
    public String decrypt(String encryptedValue) {
        if (encryptedValue == null || !encryptedValue.startsWith(FORMAT_PREFIX)) {
            throw new IllegalArgumentException("Encrypted value has an unsupported format");
        }
        byte[] encryptedBytes;
        try {
            encryptedBytes = Base64.getUrlDecoder().decode(encryptedValue.substring(FORMAT_PREFIX.length()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Encrypted value is malformed", e);
        }
        try {
            byte[] plaintextBytes = cryptography.decrypt(encryptedBytes, ASSOCIATED_DATA);
            try {
                return new String(plaintextBytes, StandardCharsets.UTF_8);
            } finally {
                Arrays.fill(plaintextBytes, (byte) 0);
            }
        } finally {
            Arrays.fill(encryptedBytes, (byte) 0);
        }
    }
}
