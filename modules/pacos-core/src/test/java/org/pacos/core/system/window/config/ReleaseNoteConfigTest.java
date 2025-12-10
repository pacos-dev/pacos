package org.pacos.core.system.window.config;

import org.junit.jupiter.api.Test;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.core.system.window.ReleaseNotePanel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ReleaseNoteConfigTest {

    private final ReleaseNoteConfig releaseNoteConfig = new ReleaseNoteConfig();

    @Test
    void whenTitleCalledThenReturnReleaseNote() {
        assertEquals("Release note", releaseNoteConfig.title());
    }

    @Test
    void whenIconCalledThenReturnSystemInfoIconUrl() {
        assertEquals(PacosIcon.SYSTEM_INFO.getUrl(), releaseNoteConfig.icon());
    }

    @Test
    void whenActivatorClassCalledThenReturnReleaseNotePanelClass() {
        assertEquals(ReleaseNotePanel.class, releaseNoteConfig.activatorClass());
    }

    @Test
    void whenIsApplicationCalledThenReturnFalse() {
        assertFalse(releaseNoteConfig.isApplication());
    }

    @Test
    void whenIsAllowMultipleInstanceCalledThenReturnFalse() {
        assertFalse(releaseNoteConfig.isAllowMultipleInstance());
    }
}
