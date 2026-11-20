package org.pacos.base.credential;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CredentialDescriptorTest {

    @Test
    void whenCreateDescriptorThenDefensivelyCopyConfiguredFields() {
        Set<String> fields = new HashSet<>(Set.of("token"));
        CredentialDescriptor descriptor = new CredentialDescriptor(
                UUID.randomUUID(), "Production API", null, "bearer-token", fields, Set.of());

        fields.add("secret");

        Set<String> configuredFields = descriptor.configuredFields();

        assertEquals(Set.of("token"), configuredFields);
        assertThrows(UnsupportedOperationException.class, () -> configuredFields.add("secret"));
    }
}
