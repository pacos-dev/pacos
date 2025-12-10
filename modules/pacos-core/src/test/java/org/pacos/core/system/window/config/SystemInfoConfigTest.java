package org.pacos.core.system.window.config;

import org.junit.jupiter.api.Test;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.core.system.window.SystemInfoConfigPanel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SystemInfoConfigTest {

    private final SystemInfoConfig systemInfoConfig = new SystemInfoConfig();

    @Test
    void whenTitleCalledThenReturnSystemInfo() {
        assertEquals("System info", systemInfoConfig.title());
    }

    @Test
    void whenIconCalledThenReturnSystemInfoIconUrl() {
        assertEquals(PacosIcon.SYSTEM_INFO.getUrl(), systemInfoConfig.icon());
    }

    @Test
    void whenActivatorClassCalledThenReturnSystemInfoConfigPanelClass() {
        assertEquals(SystemInfoConfigPanel.class, systemInfoConfig.activatorClass());
    }

    @Test
    void whenIsApplicationCalledThenReturnFalse() {
        assertFalse(systemInfoConfig.isApplication());
    }

    @Test
    void whenIsAllowMultipleInstanceCalledThenReturnFalse() {
        assertFalse(systemInfoConfig.isAllowMultipleInstance());
    }
}
