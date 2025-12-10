package org.pacos.core.component.token.service;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates unique token
 */
final class TokenGenerator {

    private static final int TOKEN_SIZE = 32;

    private TokenGenerator() {
    }

    static String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[TOKEN_SIZE];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); // Base64 URL safe encoding
    }

}
