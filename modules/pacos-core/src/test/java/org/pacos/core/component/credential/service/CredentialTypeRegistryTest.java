package org.pacos.core.component.credential.service;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.credential.service.CredentialFieldType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CredentialTypeRegistryTest {

    private final CredentialTypeRegistry registry = new CredentialTypeRegistry();

    @Test
    void whenGetAllThenReturnBuiltInCredentialTypes() {
        assertEquals(6, registry.getAll().size());
    }

    @Test
    void whenFindByKeyThenReturnMatchingDefinition() {
        var definition = registry.findByKey("oauth2-client").orElseThrow();

        assertEquals("OAuth2 Client", definition.label());
        assertTrue(definition.fields().stream()
                .anyMatch(field -> field.key().equals("scopes")
                        && field.type() == CredentialFieldType.KEY_VALUE
                        && field.multiple()));
    }

    @Test
    void whenFindByUnknownKeyThenReturnEmpty() {
        assertTrue(registry.findByKey("unknown").isEmpty());
    }

    @Test
    void whenGetRequiredWithUnknownKeyThenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> registry.getRequired("unknown"));
    }
}
