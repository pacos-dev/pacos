package org.pacos.core.component.credential.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VaultKeyProviderTest {

    @TempDir
    Path tempDir;

    @Test
    void whenNoConfiguredKeyThenGenerateAndPersistMasterKey() throws Exception {
        VaultKeyProvider provider = new VaultKeyProvider("", tempDir);

        byte[] storedKey = Files.readAllBytes(tempDir.resolve("master.key"));

        assertEquals(32, storedKey.length);
        assertArrayEquals(storedKey, provider.getMasterKey().getEncoded());
    }

    @Test
    void whenMasterKeyFileExistsThenReuseStoredKey() throws Exception {
        byte[] expectedKey = new byte[32];
        Files.createDirectories(tempDir);
        Files.write(tempDir.resolve("master.key"), expectedKey);

        VaultKeyProvider provider = new VaultKeyProvider("", tempDir);

        assertArrayEquals(expectedKey, provider.getMasterKey().getEncoded());
    }

    @Test
    void whenConfiguredKeyIsValidThenUseConfiguredKey() {
        byte[] expectedKey = new byte[32];
        String configuredKey = Base64.getEncoder().encodeToString(expectedKey);

        VaultKeyProvider provider = new VaultKeyProvider(configuredKey, tempDir);

        assertArrayEquals(expectedKey, provider.getMasterKey().getEncoded());
    }

    @Test
    void whenConfiguredKeyHasInvalidLengthThenThrowException() {
        String configuredKey = Base64.getEncoder().encodeToString(new byte[16]);

        assertThrows(IllegalStateException.class, () -> new VaultKeyProvider(configuredKey, tempDir));
    }

    @Test
    void whenConfiguredKeyDoesNotMatchPersistedKeyThenThrowException() throws Exception {
        Files.createDirectories(tempDir);
        Files.write(tempDir.resolve("master.key"), new byte[32]);
        byte[] configuredKey = new byte[32];
        configuredKey[0] = 1;
        String configured = Base64.getEncoder().encodeToString(configuredKey);

        assertThrows(IllegalStateException.class, () -> new VaultKeyProvider(configured, tempDir));
    }

    @Test
    void whenMasterKeyFileHasInvalidLengthThenThrowException() throws Exception {
        Files.createDirectories(tempDir);
        Files.write(tempDir.resolve("master.key"), new byte[16]);

        assertThrows(IllegalStateException.class, () -> new VaultKeyProvider("", tempDir));
    }
}
