package org.pacos.core.component.credential.security;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.pacos.config.property.WorkingDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Loads the Vault master key from {@code PACOS_VAULT_MASTER_KEY} or initializes a protected local key file.
 *
 * <p>Back up the configured key or the complete local key file together with the encrypted file directory.
 * Losing the key makes stored credential values and files unrecoverable.</p>
 */
@Component
public class VaultKeyProvider {

    private static final int KEY_SIZE = 32;
    private static final String KEY_ALGORITHM = "AES";
    private static final String KEY_FILE_NAME = "master.key";
    private static final String KEY_SOURCE_FILE_NAME = "master.key.source";
    private static final String LOCAL_KEY_SOURCE = "local";
    private static final String EXTERNAL_KEY_SOURCE_PREFIX = "external:";

    private final byte[] masterKey;

    @Autowired
    public VaultKeyProvider(@Value("${PACOS_VAULT_MASTER_KEY:}") String configuredKey) {
        this(configuredKey, WorkingDir.getVaultPath());
    }

    VaultKeyProvider(String configuredKey, Path vaultPath) {
        this.masterKey = configuredKey == null || configuredKey.isBlank()
                ? loadOrCreateKey(vaultPath)
                : loadConfiguredKey(configuredKey, vaultPath);
    }

    public SecretKey getMasterKey() {
        byte[] keyCopy = masterKey.clone();
        try {
            return new SecretKeySpec(keyCopy, KEY_ALGORITHM);
        } finally {
            Arrays.fill(keyCopy, (byte) 0);
        }
    }

    private static byte[] loadConfiguredKey(String configuredKey, Path vaultPath) {
        byte[] configured = decodeConfiguredKey(configuredKey);
        boolean accepted = false;
        Path keyFile = vaultPath.resolve(KEY_FILE_NAME);
        Path sourceFile = vaultPath.resolve(KEY_SOURCE_FILE_NAME);
        try {
            prepareVaultDirectory(vaultPath);
            rejectSymbolicLink(keyFile, "Vault master key cannot be a symbolic link");
            rejectSymbolicLink(sourceFile, "Vault key source marker cannot be a symbolic link");

            if (Files.exists(sourceFile, LinkOption.NOFOLLOW_LINKS)) {
                String source = readKeySource(sourceFile);
                if (source.startsWith(EXTERNAL_KEY_SOURCE_PREFIX)) {
                    String expectedFingerprint = EXTERNAL_KEY_SOURCE_PREFIX + keyFingerprint(configured);
                    if (!source.equals(expectedFingerprint)) {
                        throw new IllegalStateException("Configured Vault master key does not match the initialized external key");
                    }
                    accepted = true;
                    return configured;
                }
                if (!LOCAL_KEY_SOURCE.equals(source)) {
                    throw new IllegalStateException("PacOS Vault key source marker is invalid");
                }
                restrictFilePermissions(keyFile);
                if (!matchesPersistedKey(configured, keyFile)) {
                    throw new IllegalStateException("Configured Vault master key does not match the persisted master key");
                }
                accepted = true;
                return configured;
            }

            if (Files.exists(keyFile, LinkOption.NOFOLLOW_LINKS)) {
                restrictFilePermissions(keyFile);
                if (!matchesPersistedKey(configured, keyFile)) {
                    throw new IllegalStateException("Configured Vault master key does not match the persisted master key");
                }
                writeKeySource(sourceFile, LOCAL_KEY_SOURCE);
                accepted = true;
                return configured;
            }

            String expectedSource = EXTERNAL_KEY_SOURCE_PREFIX + keyFingerprint(configured);
            writeKeySource(sourceFile, expectedSource);
            if (!expectedSource.equals(readKeySource(sourceFile))) {
                throw new IllegalStateException("PacOS Vault was initialized with a different key source");
            }
            accepted = true;
            return configured;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot initialize configured PacOS Vault master key", e);
        } finally {
            if (!accepted) {
                Arrays.fill(configured, (byte) 0);
            }
        }
    }

    private static byte[] decodeConfiguredKey(String configuredKey) {
        byte[] key;
        try {
            key = Base64.getDecoder().decode(configuredKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("PACOS_VAULT_MASTER_KEY must be a Base64-encoded 256-bit key", e);
        }
        if (key.length != KEY_SIZE) {
            Arrays.fill(key, (byte) 0);
            throw new IllegalStateException("PACOS_VAULT_MASTER_KEY must decode to exactly 32 bytes");
        }
        return key;
    }

    private static byte[] loadOrCreateKey(Path vaultPath) {
        Path keyFile = vaultPath.resolve(KEY_FILE_NAME);
        Path sourceFile = vaultPath.resolve(KEY_SOURCE_FILE_NAME);
        byte[] key = null;
        boolean accepted = false;
        try {
            prepareVaultDirectory(vaultPath);
            rejectSymbolicLink(keyFile, "Vault master key cannot be a symbolic link");
            rejectSymbolicLink(sourceFile, "Vault key source marker cannot be a symbolic link");

            if (Files.exists(sourceFile, LinkOption.NOFOLLOW_LINKS)) {
                String source = readKeySource(sourceFile);
                if (source.startsWith(EXTERNAL_KEY_SOURCE_PREFIX)) {
                    throw new IllegalStateException(
                            "PACOS_VAULT_MASTER_KEY is required because this Vault was initialized with an external key");
                }
                if (!LOCAL_KEY_SOURCE.equals(source)) {
                    throw new IllegalStateException("PacOS Vault key source marker is invalid");
                }
                restrictFilePermissions(keyFile);
                key = readKey(keyFile);
                accepted = true;
                return key;
            }

            if (Files.exists(keyFile, LinkOption.NOFOLLOW_LINKS)) {
                restrictFilePermissions(keyFile);
                key = readKey(keyFile);
            } else {
                byte[] generatedKey = new byte[KEY_SIZE];
                new SecureRandom().nextBytes(generatedKey);
                try {
                    writeNewKeyFile(keyFile, generatedKey);
                    restrictFilePermissions(keyFile);
                    key = generatedKey;
                } catch (FileAlreadyExistsException e) {
                    Arrays.fill(generatedKey, (byte) 0);
                    rejectSymbolicLink(keyFile, "Vault master key cannot be a symbolic link");
                    restrictFilePermissions(keyFile);
                    key = readKey(keyFile);
                } catch (IOException exception) {
                    Arrays.fill(generatedKey, (byte) 0);
                    throw exception;
                }
            }
            writeKeySource(sourceFile, LOCAL_KEY_SOURCE);
            if (!LOCAL_KEY_SOURCE.equals(readKeySource(sourceFile))) {
                throw new IllegalStateException("PacOS Vault key source changed during initialization");
            }
            accepted = true;
            return key;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot initialize PacOS Vault master key", e);
        } finally {
            if (!accepted && key != null) {
                Arrays.fill(key, (byte) 0);
            }
        }
    }

    private static void writeNewKeyFile(Path keyFile, byte[] key) throws IOException {
        try {
            Files.createFile(keyFile, PosixFilePermissions.asFileAttribute(Set.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE)));
        } catch (UnsupportedOperationException exception) {
            Files.createFile(keyFile);
            restrictFilePermissions(keyFile);
        }
        try {
            Files.write(keyFile, key, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            try {
                Files.deleteIfExists(keyFile);
            } catch (IOException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }
    }

    private static void prepareVaultDirectory(Path vaultPath) throws IOException {
        rejectSymbolicLink(vaultPath, "Vault storage directory cannot be a symbolic link");
        Files.createDirectories(vaultPath);
        restrictDirectoryPermissions(vaultPath);
    }

    private static void rejectSymbolicLink(Path path, String message) {
        if (Files.isSymbolicLink(path)) {
            throw new IllegalStateException(message);
        }
    }

    private static String readKeySource(Path sourceFile) throws IOException {
        restrictFilePermissions(sourceFile);
        return Files.readString(sourceFile).trim();
    }

    private static void writeKeySource(Path sourceFile, String source) throws IOException {
        try {
            Files.writeString(sourceFile, source, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            restrictFilePermissions(sourceFile);
        } catch (FileAlreadyExistsException ignored) {
            restrictFilePermissions(sourceFile);
        }
    }

    private static String keyFingerprint(byte[] key) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(key));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static boolean matchesPersistedKey(byte[] configured, Path keyFile) throws IOException {
        byte[] persisted = readKey(keyFile);
        try {
            return MessageDigest.isEqual(configured, persisted);
        } finally {
            Arrays.fill(persisted, (byte) 0);
        }
    }

    private static byte[] readKey(Path keyFile) throws IOException {
        if (Files.size(keyFile) != KEY_SIZE) {
            throw new IllegalStateException("PacOS Vault master key file must contain exactly 32 bytes");
        }
        byte[] key = Files.readAllBytes(keyFile);
        if (key.length != KEY_SIZE) {
            Arrays.fill(key, (byte) 0);
            throw new IllegalStateException("PacOS Vault master key file must contain exactly 32 bytes");
        }
        return key;
    }

    private static void restrictDirectoryPermissions(Path directory) throws IOException {
        try {
            Files.setPosixFilePermissions(directory, Set.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE));
        } catch (UnsupportedOperationException ignored) {
        }
    }

    private static void restrictFilePermissions(Path file) throws IOException {
        try {
            Files.setPosixFilePermissions(file, Set.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException ignored) {
        }
    }
}
