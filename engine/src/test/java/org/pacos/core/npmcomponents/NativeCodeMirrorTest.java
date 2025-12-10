package org.pacos.core.npmcomponents;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NativeCodeMirrorTest {

    @Test
    void whenConstructorThenClassIsInitialized() {
        assertDoesNotThrow(NativeCodeMirror::new);
    }
}