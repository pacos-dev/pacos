package org.pacos.core.component.user.service;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.pacos.base.exception.PacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ShaHashAlgorithm {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShaHashAlgorithm.class);
    private static final String CURRENT_PREFIX = "pacos$pbkdf2-sha256$";
    private static final String CURRENT_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String LEGACY_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int CURRENT_ITERATIONS = 600_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int LEGACY_HASH_LENGTH_BYTES = 64;
    private static final int MAX_ACCEPTED_ITERATIONS = 2_000_000;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();

    private ShaHashAlgorithm() {
    }

    static String hashPassword(String password) {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        try {
            byte[] hash = deriveHash(password, salt, CURRENT_ITERATIONS, CURRENT_ALGORITHM, KEY_LENGTH_BITS);
            return CURRENT_PREFIX + CURRENT_ITERATIONS + "$" + HEX.formatHex(salt) + "$" + HEX.formatHex(hash);
        } catch (GeneralSecurityException e) {
            throw new PacosException(e);
        }
    }

    static boolean validatePassword(String originalPassword, String storedPassword) {
        if (originalPassword == null || storedPassword == null) {
            return false;
        }

        try {
            StoredHash storedHash = parseStoredHash(storedPassword);
            byte[] candidateHash = deriveHash(originalPassword, storedHash.salt(), storedHash.iterations(),
                    storedHash.algorithm(), storedHash.hash().length * Byte.SIZE);
            return MessageDigest.isEqual(storedHash.hash(), candidateHash);
        } catch (IllegalArgumentException e) {
            // Treat malformed or unsupported stored hashes as invalid credentials.
            return false;
        } catch (GeneralSecurityException e) {
            LOGGER.error("Error while validating password", e);
            return false;
        }
    }

    static boolean needsRehash(String storedPassword) {
        if (storedPassword == null) {
            return true;
        }
        try {
            StoredHash hash = parseStoredHash(storedPassword);
            return !hash.currentFormat()
                    || hash.iterations() < CURRENT_ITERATIONS
                    || hash.hash().length * Byte.SIZE != KEY_LENGTH_BITS;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static StoredHash parseStoredHash(String storedPassword) {
        if (storedPassword.startsWith(CURRENT_PREFIX)) {
            String[] parts = storedPassword.split("\\$", -1);
            if (parts.length != 5 || !parts[0].equals("pacos") || !parts[1].equals("pbkdf2-sha256")) {
                throw new IllegalArgumentException("Malformed current password hash");
            }
            int iterations = parseIterations(parts[2]);
            byte[] salt = parseHex(parts[3]);
            byte[] hash = parseHex(parts[4]);
            if (salt.length != SALT_LENGTH_BYTES || hash.length != KEY_LENGTH_BITS / Byte.SIZE) {
                throw new IllegalArgumentException("Malformed current password hash");
            }
            return new StoredHash(CURRENT_ALGORITHM, iterations, salt, hash, true);
        }

        // Backward compatibility with the legacy format: iterations:saltHex:hashHex (PBKDF2-HMAC-SHA1).
        String[] parts = storedPassword.split(":", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Unsupported password hash format");
        }
        int iterations = parseIterations(parts[0]);
        byte[] salt = parseHex(parts[1]);
        byte[] hash = parseHex(parts[2]);
        if (salt.length != SALT_LENGTH_BYTES || hash.length != LEGACY_HASH_LENGTH_BYTES) {
            throw new IllegalArgumentException("Malformed legacy password hash");
        }
        return new StoredHash(LEGACY_ALGORITHM, iterations, salt, hash, false);
    }

    private static int parseIterations(String value) {
        try {
            int iterations = Integer.parseInt(value);
            if (iterations < 1 || iterations > MAX_ACCEPTED_ITERATIONS) {
                throw new IllegalArgumentException("Unsupported password hash iteration count");
            }
            return iterations;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Malformed password hash iteration count", e);
        }
    }

    private static byte[] parseHex(String value) {
        try {
            if (value.isEmpty() || (value.length() % 2) != 0) {
                throw new IllegalArgumentException("Malformed hexadecimal password hash");
            }
            return HEX.parseHex(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Malformed hexadecimal password hash", e);
        }
    }

    private static byte[] deriveHash(String password, byte[] salt, int iterations, String algorithm, int keyLengthBits)
            throws GeneralSecurityException {
        char[] passwordChars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, iterations, keyLengthBits);
        try {
            return SecretKeyFactory.getInstance(algorithm).generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
            java.util.Arrays.fill(passwordChars, Character.MIN_VALUE);
        }
    }

    private record StoredHash(String algorithm, int iterations, byte[] salt, byte[] hash, boolean currentFormat) {
    }
}
