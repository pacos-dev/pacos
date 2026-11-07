package org.pacos.core.component.credential.security;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.pacos.config.property.WorkingDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VaultKeyProvider {

    private static final int KEY_SIZE = 32;
    private static final String KEY_ALGORITHM = "AES";
    private static final String KEY_FILE_NAME = "master.key";

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
        return new SecretKeySpec(masterKey.clone(), KEY_ALGORITHM);
    }

    private static byte[] loadConfiguredKey(String configuredKey, Path vaultPath) {
        byte[] configured = decodeConfiguredKey(configuredKey);
        if (Files.isSymbolicLink(vaultPath)) {
            throw new IllegalStateException("Vault storage directory cannot be a symbolic link");
        }
        Path keyFile = vaultPath.resolve(KEY_FILE_NAME);
        if (Files.isSymbolicLink(keyFile)) {
            throw new IllegalStateException("Vault master key cannot be a symbolic link");
        }
        if (Files.exists(keyFile, LinkOption.NOFOLLOW_LINKS)) {
            try {
                restrictFilePermissions(keyFile);
                if (!java.security.MessageDigest.isEqual(configured, readKey(keyFile))) {
                    throw new IllegalStateException("Configured Vault master key does not match the persisted master key");
                }
            } catch (IOException e) {
                throw new IllegalStateException("Cannot verify configured PacOS Vault master key", e);
            }
        }
        return configured;
    }

    private static byte[] decodeConfiguredKey(String configuredKey) {
        byte[] key;
        try {
            key = Base64.getDecoder().decode(configuredKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("PACOS_VAULT_MASTER_KEY must be a Base64-encoded 256-bit key", e);
        }
        if (key.length != KEY_SIZE) {
            throw new IllegalStateException("PACOS_VAULT_MASTER_KEY must decode to exactly 32 bytes");
        }
        return key;
    }

    private static byte[] loadOrCreateKey(Path vaultPath) {
        Path keyFile = vaultPath.resolve(KEY_FILE_NAME);
        try {
            if (Files.isSymbolicLink(vaultPath)) {
                throw new IllegalStateException("Vault storage directory cannot be a symbolic link");
            }
            Files.createDirectories(vaultPath);
            if (Files.isSymbolicLink(keyFile)) {
                throw new IllegalStateException("Vault master key cannot be a symbolic link");
            }
            restrictDirectoryPermissions(vaultPath);
            if (Files.exists(keyFile, LinkOption.NOFOLLOW_LINKS)) {
                restrictFilePermissions(keyFile);
                return readKey(keyFile);
            }

            byte[] generatedKey = new byte[KEY_SIZE];
            new SecureRandom().nextBytes(generatedKey);
            try {
                Files.write(keyFile, generatedKey, java.nio.file.StandardOpenOption.CREATE_NEW,
                        java.nio.file.StandardOpenOption.WRITE);
                restrictFilePermissions(keyFile);
                return generatedKey;
            } catch (FileAlreadyExistsException e) {
                if (Files.isSymbolicLink(keyFile)) {
                    throw new IllegalStateException("Vault master key cannot be a symbolic link");
                }
                restrictFilePermissions(keyFile);
                return readKey(keyFile);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot initialize PacOS Vault master key", e);
        }
    }

    private static byte[] readKey(Path keyFile) throws IOException {
        byte[] key = Files.readAllBytes(keyFile);
        if (key.length != KEY_SIZE) {
            throw new IllegalStateException("PacOS Vault master key file must contain exactly 32 bytes");
        }
        return key;
    }

    private static void restrictDirectoryPermissions(Path directory) throws IOException {
        try {
            Files.setPosixFilePermissions(directory,
                    Set.of(
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE,
                            PosixFilePermission.OWNER_EXECUTE));
        } catch (UnsupportedOperationException ignored) {
        }
    }

    private static void restrictFilePermissions(Path keyFile) throws IOException {
        try {
            Files.setPosixFilePermissions(keyFile,
                    Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException ignored) {
        }
    }
}
