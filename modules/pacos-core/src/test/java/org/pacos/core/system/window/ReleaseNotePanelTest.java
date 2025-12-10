package org.pacos.core.system.window;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.core.system.window.config.ReleaseNoteConfig;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ReleaseNotePanelTest {

    @Test
    void whenInitializeThenNoException() {
        VaadinMock.mockSystem();
        assertDoesNotThrow(() -> new ReleaseNotePanel(new ReleaseNoteConfig()));
    }
}