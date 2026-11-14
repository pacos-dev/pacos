package org.pacos.core.component.credential.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PluginSecretEncryptionServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void whenEncryptThenDecryptReturnsOriginalPlaintext() {
        PluginSecretEncryptionService service = createService();

        String encrypted = service.encrypt("sensitive-value");

        assertEquals("sensitive-value", service.decrypt(encrypted));
    }

    @Test
    void whenEncryptSamePlaintextTwiceThenGenerateDifferentCiphertexts() {
        PluginSecretEncryptionService service = createService();

        String first = service.encrypt("sensitive-value");
        String second = service.encrypt("sensitive-value");

        assertNotEquals(first, second);
        assertEquals("sensitive-value", service.decrypt(first));
        assertEquals("sensitive-value", service.decrypt(second));
    }

    @Test
    void whenDecryptValueWithUnsupportedVersionThenThrowException() {
        PluginSecretEncryptionService service = createService();

        assertThrows(IllegalArgumentException.class, () -> service.decrypt("v2:encrypted"));
    }

    @Test
    void whenDecryptMalformedValueThenThrowException() {
        PluginSecretEncryptionService service = createService();

        assertThrows(IllegalArgumentException.class, () -> service.decrypt("v1:not-base64!"));
    }

    @Test
    void whenDecryptTamperedValueThenThrowException() {
        PluginSecretEncryptionService service = createService();
        String encrypted = service.encrypt("sensitive-value");
        int tamperIndex = 3;
        char replacement = encrypted.charAt(tamperIndex) == 'A' ? 'B' : 'A';
        String tampered = encrypted.substring(0, tamperIndex) + replacement + encrypted.substring(tamperIndex + 1);

        assertThrows(IllegalStateException.class, () -> service.decrypt(tampered));
    }

    @Test
    void whenEncryptNullThenThrowException() {
        PluginSecretEncryptionService service = createService();

        assertThrows(IllegalArgumentException.class, () -> service.encrypt(null));
    }

    private PluginSecretEncryptionService createService() {
        return new PluginSecretEncryptionService(new VaultCryptography(new VaultKeyProvider("", tempDir)));
    }
}
