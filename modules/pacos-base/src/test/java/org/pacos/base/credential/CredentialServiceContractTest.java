package org.pacos.base.credential;

import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialServiceContractTest {

    @Test
    void whenInspectCredentialServiceThenExposeOnlyReadOperations() {
        Set<String> methods = Arrays.stream(CredentialService.class.getDeclaredMethods())
                .map(java.lang.reflect.Method::getName)
                .collect(Collectors.toSet());

        assertEquals(Set.of("findAvailable", "findById", "resolve"), methods);
    }
}
