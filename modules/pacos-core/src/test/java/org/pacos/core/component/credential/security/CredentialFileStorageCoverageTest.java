package org.pacos.core.component.credential.security;

import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CredentialFileStorageCoverageTest {

    @TempDir
    Path tempDir;

    @Test
    void whenStoreWithNullContentThenThrowIllegalArgumentException() {
        CredentialFileStorage storage = createStorage();

        assertThrows(IllegalArgumentException.class,
                () -> storage.store(UUID.randomUUID(), "certificate", null));
    }

    @Test
    void whenLoadMissingFileThenThrowIllegalStateException() {
        CredentialFileStorage storage = createStorage();

        assertThrows(IllegalStateException.class,
                () -> storage.load(UUID.randomUUID(), "certificate", UUID.randomUUID().toString()));
    }

    @Test
    void whenLoadWithMismatchedChecksumThenThrowIllegalStateException() {
        CredentialFileStorage storage = createStorage();
        UUID credentialId = UUID.randomUUID();
        StoredCredentialFile stored = storage.store(credentialId, "certificate", new byte[]{1, 2, 3});

        assertThrows(IllegalStateException.class,
                () -> storage.load(credentialId, "certificate", stored.storageReference(), 3, "wrong-checksum"));
    }

    @Test
    void whenDeleteMissingFileThenDoNothing() {
        CredentialFileStorage storage = createStorage();

        assertDoesNotThrow(() -> storage.delete(UUID.randomUUID().toString()));
    }

    @Test
    void whenDeleteWithInvalidReferenceThenThrowIllegalArgumentException() {
        CredentialFileStorage storage = createStorage();

        assertThrows(IllegalArgumentException.class, () -> storage.delete("../outside"));
    }

    private CredentialFileStorage createStorage() {
        VaultKeyProvider keyProvider = new VaultKeyProvider(
                Base64.getEncoder().encodeToString(new byte[32]), tempDir);
        return new CredentialFileStorage(new VaultCryptography(keyProvider), tempDir);
    }
}
