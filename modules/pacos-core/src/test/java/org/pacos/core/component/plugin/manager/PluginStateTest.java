package org.pacos.core.component.plugin.manager;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;

import static org.junit.jupiter.api.Assertions.*;

class PluginStateTest {
    private PluginState pluginState;
    private PluginDTO plugin1;
    private PluginDTO plugin2;

    @BeforeEach
    void setUp() {
        pluginState = new PluginState();
        plugin1 = new PluginDTO();
        plugin1.setGroupId("org.pacos");
        plugin1.setArtifactName("plugin1");
        plugin1.setVersion("1.0");
        plugin2 = new PluginDTO();
        plugin2.setGroupId("org.pacos");
        plugin2.setArtifactName("plugin2");
        plugin2.setVersion("1.0");
        pluginState.addPlugin(plugin1);
        pluginState.addPlugin(plugin2);
    }

    @Test
    void whenAddPluginThenExpectedResult() {
        assertTrue(pluginState.getPlugins().contains(plugin1));
        assertTrue(pluginState.getPlugins().contains(plugin2));
        assertEquals(PluginStatusEnum.OFF, pluginState.getState(plugin1));
        assertEquals(PluginStatusEnum.OFF, pluginState.getState(plugin2));
    }

    @Test
    void whenSetStateThenExpectedResult() {
        pluginState.setState(plugin1, PluginStatusEnum.ON);
        assertEquals(PluginStatusEnum.ON, pluginState.getState(plugin1));
    }

    @Test
    void whenRemovePluginThenExpectedResult() {
        pluginState.removePlugin(plugin1);
        assertFalse(pluginState.getPlugins().contains(plugin1));
        assertNull(pluginState.getState(plugin1));
    }

    @Test
    void whenCanRunThenExpectedResult() {
        pluginState.setState(plugin1, PluginStatusEnum.OFF);
        pluginState.setState(plugin2, PluginStatusEnum.ON);
        assertTrue(pluginState.canRun(plugin1));
        assertFalse(pluginState.canRun(plugin2));
    }

    @Test
    void whenCanStopThenExpectedResult() {
        pluginState.setState(plugin1, PluginStatusEnum.INITIALIZATION);
        pluginState.setState(plugin2, PluginStatusEnum.OFF);
        assertTrue(pluginState.canStop(plugin1));
        assertFalse(pluginState.canStop(plugin2));
    }

    @Test
    void whenPluginHasNoStateThenCanRunAndCanStopReturnFalse() {
        assertFalse(pluginState.canRun(new PluginDTO()));
        assertFalse(pluginState.canStop(new PluginDTO()));
    }

    @Test
    void whenAddExistingPluginThenPreserveCurrentState() {
        pluginState.setState(plugin1, PluginStatusEnum.ON);
        pluginState.addPlugin(plugin1);
        assertEquals(PluginStatusEnum.ON, pluginState.getState(plugin1));
    }

    @Test
    void whenGetPluginsThenExpectedResult() {
        Set<PluginDTO> plugins = pluginState.getPlugins();
        assertEquals(2, plugins.size());
        assertTrue(plugins.contains(plugin1));
        assertTrue(plugins.contains(plugin2));
    }

    @Test
    void whenIsInstallationInProgressThenExpectedResult() {
        pluginState.setState(plugin1, PluginStatusEnum.INITIALIZATION);
        Optional<PluginDTO> installationPlugin = pluginState.isInstallationInProgress();
        assertTrue(installationPlugin.isPresent());
        assertEquals(plugin1, installationPlugin.get());
    }
}
