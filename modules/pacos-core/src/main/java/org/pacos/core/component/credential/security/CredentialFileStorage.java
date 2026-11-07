package org.pacos.core.component.credential.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

import org.pacos.config.property.WorkingDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CredentialFileStorage {

    private static final String FILES_DIRECTORY = "files";

    private final VaultCryptography cryptography;
    private final Path vaultPath;

    @Autowired
    public CredentialFileStorage(VaultCryptography cryptography) {
        this(cryptography, WorkingDir.getVaultPath());
    }

    CredentialFileStorage(VaultCryptography cryptography, Path vaultPath) {
        this.cryptography = cryptography;
        this.vaultPath = vaultPath;
    }

    public StoredCredentialFile store(UUID credentialId, String fieldKey, byte[] content) {
        if (content == null) {
            throw new IllegalArgumentException("Credential file content is required");
        }
        String storageReference = UUID.randomUUID().toString();
        byte[] encrypted = cryptography.encrypt(content, associatedData(credentialId, fieldKey));
        Path file = resolvePath(storageReference);
        try {
            Files.createDirectories(file.getParent());
            restrictDirectoryPermissions(vaultPath);
            restrictDirectoryPermissions(file.getParent());
            Files.write(file, encrypted, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            restrictFilePermissions(file);
            return new StoredCredentialFile(storageReference, content.length, checksum(content));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot store encrypted credential file", e);
        }
    }

    public byte[] load(UUID credentialId, String fieldKey, String storageReference) {
        return load(credentialId, fieldKey, storageReference, -1, null);
    }

    public byte[] load(
            UUID credentialId, String fieldKey, String storageReference, long expectedSize, String expectedChecksum) {
        Path file = resolvePath(storageReference);
        try {
            if (Files.isSymbolicLink(file)) {
                throw new IllegalStateException("Vault credential files cannot be symbolic links");
            }
            byte[] encrypted = Files.readAllBytes(file);
            byte[] plaintext = cryptography.decrypt(encrypted, associatedData(credentialId, fieldKey));
            if (expectedSize >= 0 && plaintext.length != expectedSize
                    || expectedChecksum != null && !checksum(plaintext).equals(expectedChecksum)) {
                java.util.Arrays.fill(plaintext, (byte) 0);
                throw new IllegalStateException("Credential file metadata does not match stored content");
            }
            return plaintext;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read encrypted credential file", e);
        }
    }

    public void delete(String storageReference) {
        try {
            Files.deleteIfExists(resolvePath(storageReference));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot delete encrypted credential file", e);
        }
    }

    private Path resolvePath(String storageReference) {
        UUID fileId;
        try {
            fileId = UUID.fromString(storageReference);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid credential file reference", e);
        }
        if (Files.isSymbolicLink(vaultPath)) {
            throw new IllegalStateException("Vault storage directory cannot be a symbolic link");
        }
        Path filesPath = vaultPath.resolve(FILES_DIRECTORY).normalize();
        if (Files.isSymbolicLink(filesPath)) {
            throw new IllegalStateException("Vault files directory cannot be a symbolic link");
        }
        Path filePath = filesPath.resolve(fileId + ".bin").normalize();
        if (!filePath.startsWith(filesPath)) {
            throw new IllegalArgumentException("Credential file path escapes Vault storage");
        }
        return filePath;
    }

    private static void restrictDirectoryPermissions(Path directory) throws IOException {
        try {
            Files.setPosixFilePermissions(directory,
                    java.util.Set.of(
                            java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                            java.nio.file.attribute.PosixFilePermission.OWNER_WRITE,
                            java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE));
        } catch (UnsupportedOperationException ignored) {
        }
    }

    private static void restrictFilePermissions(Path file) throws IOException {
        try {
            Files.setPosixFilePermissions(file,
                    java.util.Set.of(
                            java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                            java.nio.file.attribute.PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException ignored) {
        }
    }

    private static byte[] associatedData(UUID credentialId, String fieldKey) {
        return (credentialId + ":" + fieldKey + ":file").getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private static String checksum(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
