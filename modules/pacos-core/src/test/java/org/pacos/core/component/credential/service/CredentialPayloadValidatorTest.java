package org.pacos.core.component.credential.service;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.credential.repository.CredentialFileRepository;
import org.pacos.core.component.credential.repository.CredentialValueRepository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class CredentialPayloadValidatorTest {

    private final CredentialPayloadValidator validator = new CredentialPayloadValidator(
            mock(CredentialValueRepository.class), mock(CredentialFileRepository.class), 1024);

    @Test
    void whenSingleValueKeyValueFieldContainsMultipleEntriesThenRejectPayload() {
        CredentialTypeDefinition definition = definition(false);

        Map<String, Object> values = Map.of("pairs", Map.of("first", "1", "second", "2"));
        Map<String, CredentialFileInput> files = Map.of();

        assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(definition, values, files));
    }

    @Test
    void whenKeyValueFieldContainsBlankKeyThenRejectPayload() {
        CredentialTypeDefinition definition = definition(true);

        Map<String, Object> values = Map.of("pairs", Map.of(" ", "value"));
        Map<String, CredentialFileInput> files = Map.of();

        assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(definition, values, files));
    }

    @Test
    void whenNumberFieldContainsNaNThenRejectPayload() {
        CredentialTypeDefinition definition = new CredentialTypeDefinition(
                "numeric", "Numeric", null,
                List.of(new CredentialFieldDefinition("value", "Value", CredentialFieldType.NUMBER,
                        false, false, false)));

        Map<String, Object> values = Map.of("value", Double.NaN);
        Map<String, CredentialFileInput> files = Map.of();

        assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(definition, values, files));
    }

    @Test
    void whenMultipleKeyValueFieldContainsSeveralEntriesThenAcceptPayload() {
        CredentialTypeDefinition definition = definition(true);

        assertDoesNotThrow(() -> validator.validateCreate(
                definition, Map.of("pairs", Map.of("first", "1", "second", "2")), Map.of()));
    }

    private static CredentialTypeDefinition definition(boolean multiple) {
        return new CredentialTypeDefinition(
                "pairs", "Pairs", null,
                List.of(new CredentialFieldDefinition("pairs", "Pairs", CredentialFieldType.KEY_VALUE,
                        false, false, multiple)));
    }
}
