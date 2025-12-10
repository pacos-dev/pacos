package org.pacos.base.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ModuleEventTest {
    @Test
    void testEnumValuesExist() {
        assertNotNull(ModuleEvent.MODULE_SHUTDOWN, "MODULE_SHUTDOWN should be a valid enum value");
        assertNotNull(ModuleEvent.MODULE_OPENED, "MODULE_OPENED should be a valid enum value");
        assertNotNull(ModuleEvent.RESTART_REQUIRED, "RESTART_REQUIRED should be a valid enum value");
    }

    @Test
    void testEnumValueOf() {
        assertEquals(ModuleEvent.MODULE_SHUTDOWN, ModuleEvent.valueOf("MODULE_SHUTDOWN"));
        assertEquals(ModuleEvent.MODULE_OPENED, ModuleEvent.valueOf("MODULE_OPENED"));
        assertEquals(ModuleEvent.RESTART_REQUIRED, ModuleEvent.valueOf("RESTART_REQUIRED"));
    }
}