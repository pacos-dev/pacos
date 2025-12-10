package org.pacos.core.component.plugin.service.data;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.dto.PluginDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PluginUpdateResultTest {

    @Test
    void whenUpdateResultCalledThenCorrectSummary() {
        PluginDTO plugin1 = new PluginDTO(new Plugin("org.pacos", "plugin", "PluginA", "1.0", "", ""));
        PluginDTO plugin2 = new PluginDTO(new Plugin("org.pacos", "plugin", "PluginB", "2.0", "", ""));
        PluginDTO plugin3 = new PluginDTO(new Plugin("org.pacos", "plugin", "PluginC", "3.0", "", ""));
        plugin3.setErrMsg("Connection error");

        List<PluginDTO> updated = List.of(plugin1, plugin2);
        List<PluginDTO> failed = List.of(plugin3);

        PluginUpdateResult result = new PluginUpdateResult(updated, failed);

        String expected = """
                Updated plugins: 2
                \tPlugin='PluginA' | version='1.0'
                \tPlugin='PluginB' | version='2.0'
                Failed plugins update: 1
                \tPlugin='PluginC' | version='3.0' | errorMsg='Connection error'
                """;

        assertEquals(expected.replaceAll("\\s", ""), result.updateResult().replaceAll("\\s", ""));
    }

    @Test
    void whenUpdateResultCalledWithEmptyListsThenEmptySummary() {
        PluginUpdateResult result = new PluginUpdateResult(List.of(), List.of());

        String expected = """
                Updated plugins: 0
                Failed plugins update: 0
                """;

        assertEquals(expected.replaceAll("\\s", ""), result.updateResult().replaceAll("\\s", ""));
    }
}
