package org.pacos.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ConfigViewTest {

    @Test
    void whenConstructorThenClassIsInitialized(){
        assertDoesNotThrow(ConfigView::new);
    }

}