package org.pacos.core.component.plugin.manager;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PluginStateTest {

    private PluginDTO plugin1;
    private PluginDTO plugin2;

    @BeforeEach
    public void setUp() {
        plugin1 = new PluginDTO();
        plugin1.setArtifactName("plugin1");
        plugin2 = new PluginDTO();
        plugin2.setArtifactName("plugin2");
        PluginState.getPlugins().forEach(PluginState::removePlugin);
        PluginState.addPlugin(plugin1);
        PluginState.addPlugin(plugin2);
    }

    @Test
    public void whenAddPluginThenExpectedResult() {
        assertTrue(PluginState.getPlugins().contains(plugin1));
        assertTrue(PluginState.getPlugins().contains(plugin2));
        assertEquals(PluginStatusEnum.OFF, PluginState.getState(plugin1));
        assertEquals(PluginStatusEnum.OFF, PluginState.getState(plugin2));
    }

    @Test
    public void whenSetStateThenExpectedResult() {
        PluginState.setState(plugin1, PluginStatusEnum.ON);
        assertEquals(PluginStatusEnum.ON, PluginState.getState(plugin1));
    }

    @Test
    public void whenRemovePluginThenExpectedResult() {
        PluginState.removePlugin(plugin1);
        assertFalse(PluginState.getPlugins().contains(plugin1));
        assertNull(PluginState.getState(plugin1));
    }

    @Test
    public void whenCanRunThenExpectedResult() {
        PluginState.setState(plugin1, PluginStatusEnum.OFF);
        PluginState.setState(plugin2, PluginStatusEnum.ON);
        assertTrue(PluginState.canRun(plugin1));
        assertFalse(PluginState.canRun(plugin2));
    }

    @Test
    public void whenCanStopThenExpectedResult() {
        PluginState.setState(plugin1, PluginStatusEnum.INITIALIZATION);
        PluginState.setState(plugin2, PluginStatusEnum.OFF);
        assertTrue(PluginState.canStop(plugin1));
        assertFalse(PluginState.canStop(plugin2));
    }

    @Test
    public void whenGetPluginsThenExpectedResult() {
        Set<PluginDTO> plugins = PluginState.getPlugins();
        assertEquals(2, plugins.size());
        assertTrue(plugins.contains(plugin1));
        assertTrue(plugins.contains(plugin2));
    }

    @Test
    public void whenIsInstallationInProgressThenExpectedResult() {
        PluginState.setState(plugin1, PluginStatusEnum.INITIALIZATION);
        Optional<PluginDTO> installationPlugin = PluginState.isInstallationInProgress();
        assertTrue(installationPlugin.isPresent());
        assertEquals(plugin1, installationPlugin.get());
    }
}
