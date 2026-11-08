package org.pacos.base.credential;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CredentialReferenceTest {

    @Test
    void whenCreateReferenceThenPreserveIdentifier() {
        UUID id = UUID.randomUUID();

        assertEquals(id, new CredentialReference(id).id());
    }

    @Test
    void whenCreateReferenceFromInvalidStringThenThrowException() {
        assertThrows(IllegalArgumentException.class, () -> CredentialReference.of("invalid"));
    }
}
