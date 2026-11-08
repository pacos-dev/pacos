package org.pacos.core.component.credential.security;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VaultCryptographyTest {

    @TempDir
    Path tempDir;

    @Test
    void whenEncryptThenDecryptReturnsOriginalPlaintext() {
        VaultCryptography cryptography = createCryptography();
        byte[] plaintext = "vault-secret".getBytes(StandardCharsets.UTF_8);
        byte[] associatedData = "credential-id:field-key".getBytes(StandardCharsets.UTF_8);

        byte[] encrypted = cryptography.encrypt(plaintext, associatedData);

        assertArrayEquals(plaintext, cryptography.decrypt(encrypted, associatedData));
    }

    @Test
    void whenEncryptSamePlaintextTwiceThenDifferentCiphertextsReturned() {
        VaultCryptography cryptography = createCryptography();
        byte[] plaintext = "vault-secret".getBytes(StandardCharsets.UTF_8);
        byte[] associatedData = "credential-id:field-key".getBytes(StandardCharsets.UTF_8);

        byte[] first = cryptography.encrypt(plaintext, associatedData);
        byte[] second = cryptography.encrypt(plaintext, associatedData);

        assertNotEquals(Base64.getEncoder().encodeToString(first),
                Base64.getEncoder().encodeToString(second));
    }

    @Test
    void whenDecryptWithDifferentAssociatedDataThenThrowException() {
        VaultCryptography cryptography = createCryptography();
        byte[] encrypted = cryptography.encrypt("secret".getBytes(StandardCharsets.UTF_8),
                "credential-a:password".getBytes(StandardCharsets.UTF_8));

        assertThrows(IllegalStateException.class, () -> cryptography.decrypt(encrypted,
                "credential-b:password".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void whenDecryptTamperedDataThenThrowException() {
        VaultCryptography cryptography = createCryptography();
        byte[] encrypted = cryptography.encrypt("secret".getBytes(StandardCharsets.UTF_8),
                "credential-id:password".getBytes(StandardCharsets.UTF_8));
        encrypted[encrypted.length - 1] ^= 1;

        assertThrows(IllegalStateException.class, () -> cryptography.decrypt(encrypted,
                "credential-id:password".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void whenDecryptInvalidDataThenThrowException() {
        VaultCryptography cryptography = createCryptography();

        assertThrows(IllegalArgumentException.class, () -> cryptography.decrypt(new byte[8], new byte[0]));
    }

    @Test
    void whenEncryptEmptyPlaintextThenDecryptReturnsEmptyPlaintext() {
        VaultCryptography cryptography = createCryptography();
        byte[] encrypted = cryptography.encrypt(new byte[0], new byte[0]);

        assertArrayEquals(new byte[0], cryptography.decrypt(encrypted, new byte[0]));
    }

    private VaultCryptography createCryptography() {
        byte[] key = new byte[32];
        Arrays.fill(key, (byte) 7);
        VaultKeyProvider keyProvider = new VaultKeyProvider(Base64.getEncoder().encodeToString(key), tempDir);
        return new VaultCryptography(keyProvider);
    }
}
