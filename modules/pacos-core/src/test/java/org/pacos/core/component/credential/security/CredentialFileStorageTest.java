package org.pacos.core.component.credential.security;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CredentialFileStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void whenStoreThenEncryptedFileCanBeLoaded() {
        CredentialFileStorage storage = createStorage();
        UUID credentialId = UUID.randomUUID();
        byte[] content = "certificate-content".getBytes(StandardCharsets.UTF_8);

        StoredCredentialFile stored = storage.store(credentialId, "certificate", content);

        assertArrayEquals(content, storage.load(credentialId, "certificate", stored.storageReference()));
    }

    @Test
    void whenStoreThenFileDoesNotContainPlaintext() throws Exception {
        CredentialFileStorage storage = createStorage();
        UUID credentialId = UUID.randomUUID();
        byte[] content = "private-key-content".getBytes(StandardCharsets.UTF_8);

        StoredCredentialFile stored = storage.store(credentialId, "privateKey", content);
        Path encryptedFile = tempDir.resolve("files").resolve(stored.storageReference() + ".bin");

        assertTrue(Files.exists(encryptedFile));
        assertFalse(new String(Files.readAllBytes(encryptedFile), StandardCharsets.UTF_8).contains("private-key-content"));
    }

    @Test
    void whenLoadWithMismatchedMetadataThenThrowException() {
        CredentialFileStorage storage = createStorage();
        UUID credentialId = UUID.randomUUID();
        StoredCredentialFile stored = storage.store(credentialId, "certificate", new byte[]{1, 2, 3});

        assertThrows(IllegalStateException.class,
                () -> storage.load(credentialId, "certificate", stored.storageReference(), 4, stored.checksum()));
    }

    @Test
    void whenEncryptedFileIsTruncatedThenRejectItUsingStoredSize() throws Exception {
        CredentialFileStorage storage = createStorage();
        UUID credentialId = UUID.randomUUID();
        StoredCredentialFile stored = storage.store(credentialId, "certificate", new byte[]{1, 2, 3});
        Path encryptedFile = tempDir.resolve("files").resolve(stored.storageReference() + ".bin");
        Files.write(encryptedFile, new byte[]{1, 2, 3});

        assertThrows(IllegalStateException.class,
                () -> storage.load(credentialId, "certificate", stored.storageReference(),
                        stored.size(), stored.checksum()));
    }

    @Test
    void whenLoadWithDifferentCredentialIdThenThrowException() {
        CredentialFileStorage storage = createStorage();
        StoredCredentialFile stored = storage.store(UUID.randomUUID(), "certificate", new byte[]{1, 2, 3});

        assertThrows(IllegalStateException.class,
                () -> storage.load(UUID.randomUUID(), "certificate", stored.storageReference()));
    }

    @Test
    void whenDeleteThenStoredFileIsRemoved() throws Exception {
        CredentialFileStorage storage = createStorage();
        StoredCredentialFile stored = storage.store(UUID.randomUUID(), "certificate", new byte[]{1, 2, 3});
        Path encryptedFile = tempDir.resolve("files").resolve(stored.storageReference() + ".bin");

        storage.delete(stored.storageReference());

        assertFalse(Files.exists(encryptedFile));
    }

    @Test
    void whenLoadWithInvalidStorageReferenceThenThrowException() {
        CredentialFileStorage storage = createStorage();

        assertThrows(IllegalArgumentException.class, () -> storage.load(UUID.randomUUID(), "certificate", "../outside"));
    }

    private CredentialFileStorage createStorage() {
        byte[] key = new byte[32];
        VaultKeyProvider keyProvider = new VaultKeyProvider(Base64.getEncoder().encodeToString(key), tempDir);
        return new CredentialFileStorage(new VaultCryptography(keyProvider), tempDir);
    }
}
