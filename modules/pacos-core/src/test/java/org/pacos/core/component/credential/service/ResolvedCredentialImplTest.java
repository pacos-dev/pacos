package org.pacos.core.component.credential.service;

import java.io.InputStream;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.pacos.base.credential.ResolvedCredential;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResolvedCredentialImplTest {

    @Test
    void whenGetTextThenReturnStoredText() {
        ResolvedCredential credential = new ResolvedCredentialImpl(Map.of("username", "api-user"), Map.of());

        assertEquals("api-user", credential.getText("username"));
        assertNull(credential.getText("missing"));
    }

    @Test
    void whenGetSecretThenReturnDefensiveCopy() {
        char[] secret = "secret".toCharArray();
        ResolvedCredential credential = new ResolvedCredentialImpl(Map.of("password", secret), Map.of());

        char[] first = credential.getSecret("password");
        first[0] = 'X';

        assertArrayEquals(secret, credential.getSecret("password"));
    }

    @Test
    void whenGetKeyValuesThenReturnImmutableMap() {
        ResolvedCredential credential = new ResolvedCredentialImpl(
                Map.of("headers", Map.of("X-Api-Key", "secret")), Map.of());

        Map<String, String> values = credential.getKeyValues("headers");

        assertEquals(Map.of("X-Api-Key", "secret"), values);
        assertThrows(UnsupportedOperationException.class, () -> values.put("Authorization", "Bearer token"));
    }

    @Test
    void whenGetValueWithWrongTypeThenThrowIllegalArgumentException() {
        ResolvedCredential credential = new ResolvedCredentialImpl(Map.of("secret", "plain-text"), Map.of());

        assertThrows(IllegalArgumentException.class, () -> credential.getSecret("secret"));
        assertThrows(IllegalArgumentException.class, () -> credential.getKeyValues("secret"));
    }

    @Test
    void whenOpenFileThenReturnFileContents() throws Exception {
        ResolvedCredential credential = new ResolvedCredentialImpl(
                Map.of(), Map.of("certificate", () -> new byte[]{1, 2, 3}));

        try (InputStream input = credential.openFile("certificate")) {
            assertArrayEquals(new byte[]{1, 2, 3}, input.readAllBytes());
        }
        assertNull(credential.openFile("missing"));
    }

    @Test
    void whenCloseThenRejectFurtherAccess() {
        ResolvedCredential credential = new ResolvedCredentialImpl(Map.of("username", "api-user"), Map.of());

        credential.close();

        assertThrows(IllegalStateException.class, () -> credential.getText("username"));
        assertThrows(IllegalStateException.class, () -> credential.getSecret("password"));
        assertThrows(IllegalStateException.class, () -> credential.getKeyValues("headers"));
        assertThrows(IllegalStateException.class, () -> credential.openFile("certificate"));
    }

    @Test
    void whenCloseThenClearStoredSecret() {
        char[] secret = "sensitive".toCharArray();
        ResolvedCredential credential = new ResolvedCredentialImpl(Map.of("password", secret), Map.of());

        credential.close();

        assertArrayEquals(new char[secret.length], secret);
    }

    @Test
    void whenCloseTwiceThenRemainClosed() {
        ResolvedCredential credential = new ResolvedCredentialImpl(Map.of(), Map.of());

        credential.close();
        credential.close();

        assertThrows(IllegalStateException.class, () -> credential.getText("missing"));
    }

    @Test
    void whenCloseThenCloseOpenedFileStreams() throws Exception {
        ResolvedCredential credential = new ResolvedCredentialImpl(
                Map.of(), Map.of("certificate", () -> new byte[]{1, 2, 3}));
        InputStream input = credential.openFile("certificate");

        credential.close();

        assertEquals(-1, input.read());
    }
}
