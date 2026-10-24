package org.pacos.core.component.user.service;

import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class ShaHashAlgorithmTest {
    private static final HexFormat HEX = HexFormat.of();

    @Test
    void whenHashPasswordThenUseCurrentFormatAndRequireNoRehash() {
        String hash = ShaHashAlgorithm.hashPassword("test_password");

        assertTrue(hash.startsWith("pacos$pbkdf2-sha256$600000$"));
        assertTrue(ShaHashAlgorithm.validatePassword("test_password", hash));
        assertFalse(ShaHashAlgorithm.needsRehash(hash));
    }

    @Test
    void whenHashPasswordTwiceThenHashIsDifferent() {
        String hash1 = ShaHashAlgorithm.hashPassword("test_password");
        String hash2 = ShaHashAlgorithm.hashPassword("test_password");

        assertNotEquals(hash1, hash2);
    }

    @Test
    void whenValidatePasswordDoesNotMatchThenReturnFalse() {
        String hash = ShaHashAlgorithm.hashPassword("test_password");

        assertFalse(ShaHashAlgorithm.validatePassword("wrong_password", hash));
    }

    @Test
    void whenValidateLegacyPasswordThenAcceptAndRequireRehash() throws Exception {
        String legacyHash = legacyPasswordHash("test_password");

        assertTrue(ShaHashAlgorithm.validatePassword("test_password", legacyHash));
        assertFalse(ShaHashAlgorithm.validatePassword("wrong_password", legacyHash));
        assertTrue(ShaHashAlgorithm.needsRehash(legacyHash));
    }

    @Test
    void whenStoredHashIsMalformedThenRejectAndRequireRehash() {
        assertFalse(ShaHashAlgorithm.validatePassword("test_password", "not-a-valid-hash"));
        assertTrue(ShaHashAlgorithm.needsRehash("not-a-valid-hash"));
    }

    @Test
    void whenPasswordHashAlgorithmIsUnavailableThenValidationReturnsFalse() {
        String hash = ShaHashAlgorithm.hashPassword("test_password");
        try (MockedStatic<SecretKeyFactory> mock = mockStatic(SecretKeyFactory.class)) {
            mock.when(() -> SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"))
                    .thenThrow(NoSuchAlgorithmException.class);

            assertFalse(ShaHashAlgorithm.validatePassword("test_password", hash));
        }
    }

    @Test
    void whenStoredHashHasUnexpectedSaltOrHashLengthThenReject() {
        String shortCurrentSalt = "pacos$pbkdf2-sha256$600000$" + "00".repeat(8) + "$" + "00".repeat(32);
        String shortCurrentHash = "pacos$pbkdf2-sha256$600000$" + "00".repeat(16) + "$00";
        String shortLegacyHash = "100:" + "00".repeat(16) + ":00";

        for (String malformedHash : new String[]{shortCurrentSalt, shortCurrentHash, shortLegacyHash}) {
            assertFalse(ShaHashAlgorithm.validatePassword("test_password", malformedHash));
            assertTrue(ShaHashAlgorithm.needsRehash(malformedHash));
        }
    }

    private static String legacyPasswordHash(String password) throws Exception {
        byte[] salt = new byte[16];
        for (int i = 0; i < salt.length; i++) {
            salt[i] = (byte) i;
        }
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 100, 64 * Byte.SIZE);
        byte[] hash;
        try {
            hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
        return "100:" + HEX.formatHex(salt) + ":" + HEX.formatHex(hash);
    }
}
