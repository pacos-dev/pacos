package org.pacos.core.component.credential.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class CredentialFileInputTest {

    @Test
    void whenClearThenZeroInternalContent() {
        CredentialFileInput input = new CredentialFileInput("private.key", null, new byte[]{1, 2, 3});

        input.clear();

        assertArrayEquals(new byte[]{0, 0, 0}, input.content());
    }
}
