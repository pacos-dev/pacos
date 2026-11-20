package org.pacos.core.component.credential.service;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import org.pacos.core.component.credential.service.CredentialFieldType;
import org.pacos.base.utils.ObjectMapperUtils;
import org.pacos.core.component.credential.domain.CredentialValueEntity;
import org.pacos.core.component.credential.security.VaultCryptography;

/**
 * Encodes and decodes credential values and protects their persisted representation using field-bound
 * associated data.
 */
final class CredentialValueCodec {

    private final VaultCryptography cryptography;

    CredentialValueCodec(VaultCryptography cryptography) {
        this.cryptography = cryptography;
    }

    byte[] encrypt(UUID credentialId, String fieldKey, CredentialFieldType fieldType, Object value) {
        byte[] plaintext = encode(fieldType, value);
        try {
            return cryptography.encrypt(plaintext, associatedData(credentialId, fieldKey, fieldType));
        } finally {
            Arrays.fill(plaintext, (byte) 0);
            if (value instanceof char[] secret) {
                Arrays.fill(secret, '\0');
            }
        }
    }

    Object decrypt(UUID credentialId, CredentialValueEntity value) {
        byte[] plaintext = cryptography.decrypt(
                value.getEncryptedValue(),
                associatedData(credentialId, value.getFieldKey(), value.getFieldType()));
        try {
            return decode(value.getFieldType(), plaintext);
        } finally {
            Arrays.fill(plaintext, (byte) 0);
        }
    }

    private static byte[] encode(CredentialFieldType type, Object value) {
        try {
            return switch (type) {
                case TEXT -> ((String) value).getBytes(StandardCharsets.UTF_8);
                case SECRET -> encodeSecret((char[]) value);
                case KEY_VALUE, BOOLEAN, NUMBER -> ObjectMapperUtils.getMapper().writeValueAsBytes(value);
                case FILE -> throw new IllegalArgumentException("File fields must use Vault file storage");
            };
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Cannot encode credential value", e);
        }
    }

    private static Object decode(CredentialFieldType fieldType, byte[] plaintext) {
        try {
            return switch (fieldType) {
                case TEXT -> new String(plaintext, StandardCharsets.UTF_8);
                case SECRET -> decodeSecret(plaintext);
                case KEY_VALUE -> ObjectMapperUtils.getMapper().readValue(
                        plaintext, new TypeReference<Map<String, String>>() {});
                case BOOLEAN -> ObjectMapperUtils.getMapper().readValue(plaintext, Boolean.class);
                case NUMBER -> ObjectMapperUtils.getMapper().readValue(plaintext, BigDecimal.class);
                case FILE -> throw new IllegalStateException("File values must be stored in Vault file storage");
            };
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot decode credential value", e);
        }
    }

    private static byte[] encodeSecret(char[] secret) {
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(secret));
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        if (buffer.hasArray()) {
            Arrays.fill(buffer.array(), (byte) 0);
        }
        return result;
    }

    private static char[] decodeSecret(byte[] plaintext) {
        CharBuffer decoded = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(plaintext));
        char[] secret = new char[decoded.remaining()];
        decoded.get(secret);
        if (decoded.hasArray()) {
            Arrays.fill(decoded.array(), '\0');
        }
        return secret;
    }

    private static byte[] associatedData(UUID credentialId, String fieldKey, CredentialFieldType fieldType) {
        return (credentialId + ":" + fieldKey + ":" + fieldType.name() + ":value")
                .getBytes(StandardCharsets.UTF_8);
    }
}
