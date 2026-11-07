package org.pacos.core.component.credential.security;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;

import org.springframework.stereotype.Component;

/** Provides authenticated encryption for Vault values and files. */
@Component
public class VaultCryptography {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int NONCE_SIZE = 12;
    private static final int TAG_SIZE_BITS = 128;
    private static final int TAG_SIZE_BYTES = TAG_SIZE_BITS / 8;

    private final VaultKeyProvider keyProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Creates the Vault cryptography service.
     * @param keyProvider provider of the master encryption key
     */
    public VaultCryptography(VaultKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    /**
     * Encrypts plaintext with AES-GCM and authenticates the supplied associated data.
     * @param plaintext data to encrypt
     * @param associatedData contextual data authenticated but not encrypted
     * @return nonce followed by authenticated ciphertext
     */
    public byte[] encrypt(byte[] plaintext, byte[] associatedData) {
        if (plaintext == null) {
            throw new IllegalArgumentException("Plaintext is required");
        }
        byte[] nonce = new byte[NONCE_SIZE];
        secureRandom.nextBytes(nonce);
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keyProvider.getMasterKey(), new GCMParameterSpec(TAG_SIZE_BITS, nonce));
            if (associatedData != null) {
                cipher.updateAAD(associatedData);
            }
            byte[] encrypted = cipher.doFinal(plaintext);
            byte[] result = Arrays.copyOf(nonce, nonce.length + encrypted.length);
            System.arraycopy(encrypted, 0, result, nonce.length, encrypted.length);
            return result;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot encrypt Vault data", e);
        }
    }

    /**
     * Decrypts and authenticates AES-GCM data.
     * @param encryptedData nonce followed by authenticated ciphertext
     * @param associatedData contextual data used during encryption
     * @return decrypted plaintext
     */
    public byte[] decrypt(byte[] encryptedData, byte[] associatedData) {
        if (encryptedData == null || encryptedData.length < NONCE_SIZE + TAG_SIZE_BYTES) {
            throw new IllegalArgumentException("Encrypted Vault data is invalid");
        }
        byte[] nonce = Arrays.copyOfRange(encryptedData, 0, NONCE_SIZE);
        byte[] ciphertext = Arrays.copyOfRange(encryptedData, NONCE_SIZE, encryptedData.length);
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keyProvider.getMasterKey(), new GCMParameterSpec(TAG_SIZE_BITS, nonce));
            if (associatedData != null) {
                cipher.updateAAD(associatedData);
            }
            return cipher.doFinal(ciphertext);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot decrypt Vault data or authentication failed", e);
        }
    }
}
