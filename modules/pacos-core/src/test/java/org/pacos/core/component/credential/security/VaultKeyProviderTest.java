package org.pacos.core.component.credential.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Assumptions;

import java.nio.file.attribute.PosixFilePermission;

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
    void whenGeneratedMasterKeyThenRestrictPermissionsAtFileCreation() throws Exception {
        new VaultKeyProvider("", tempDir);
        Assumptions.assumeTrue(Files.getFileStore(tempDir).supportsFileAttributeView("posix"));

        assertEquals(Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE),
                Files.getPosixFilePermissions(tempDir.resolve("master.key")));
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
    @Test
    void whenExternalKeyWasInitializedAndConfigurationIsMissingThenThrowException() {
        String configuredKey = Base64.getEncoder().encodeToString(new byte[32]);
        new VaultKeyProvider(configuredKey, tempDir);

        assertThrows(IllegalStateException.class, () -> new VaultKeyProvider("", tempDir));
    }

    @Test
    void whenExternalKeyChangesAfterInitializationThenThrowException() {
        byte[] firstKey = new byte[32];
        byte[] secondKey = new byte[32];
        secondKey[0] = 1;
        new VaultKeyProvider(Base64.getEncoder().encodeToString(firstKey), tempDir);

        assertThrows(IllegalStateException.class,
                () -> new VaultKeyProvider(Base64.getEncoder().encodeToString(secondKey), tempDir));
    }

    @Test
    void whenLegacyLocalKeyExistsWithoutSourceMarkerThenReuseAndMarkItAsLocal() throws Exception {
        byte[] expectedKey = new byte[32];
        expectedKey[0] = 7;
        Files.createDirectories(tempDir);
        Files.write(tempDir.resolve("master.key"), expectedKey);

        VaultKeyProvider provider = new VaultKeyProvider("", tempDir);

        assertArrayEquals(expectedKey, provider.getMasterKey().getEncoded());
        assertEquals("local", Files.readString(tempDir.resolve("master.key.source")));
    }

}
