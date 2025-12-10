package org.pacos.core.component.installer.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstallationModeTest {

    @Test
    void whenIsSingleCalledOnSingleThenReturnTrue() {
        assertTrue(InstallationMode.SINGLE.isSingle());
    }

    @Test
    void whenIsSingleCalledOnMultiThenReturnFalse() {
        assertFalse(InstallationMode.MULTI.isSingle());
    }
}
