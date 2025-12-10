package org.pacos.core.component.plugin.view.plugin;

import org.junit.jupiter.api.Test;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.dto.PluginDTO;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PluginRowLineTest {
    @Test
    void whenInitializeForPluginWithoutIconThenNoException() {
        PluginDTO plugin1 = new PluginDTO(new Plugin("plugin1", "test", "", "1.0", "", ""));
        assertDoesNotThrow(() -> new PluginRowLine(plugin1));
    }

    @Test
    void whenInitializeForPluginWithIconThenNoException() {
        PluginDTO plugin1 = new PluginDTO(new Plugin("plugin1", "test", "", "1.0", "", ""));
        plugin1.setIcon("icon");
        assertDoesNotThrow(() -> new PluginRowLine(plugin1));
    }
}