package org.pacos.core.component.token.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TokenGeneratorTest {

    @Test
    void whenGenerateThenReturnRandomString() {
        assertNotEquals(TokenGenerator.generateToken(), TokenGenerator.generateToken());
    }
}