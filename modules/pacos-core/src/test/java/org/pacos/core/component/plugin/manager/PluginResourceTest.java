package org.pacos.core.component.plugin.manager;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.data.PluginDataLoader;
import org.pacos.core.component.plugin.manager.data.PluginJar;
import org.pacos.core.component.plugin.manager.data.RequestMapping;
import org.springframework.context.ApplicationContext;
import org.vaadin.addons.variablefield.provider.VariableProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PluginResourceTest {

    private ApplicationContext coreContext;
    private PluginResource pluginResource;

    @BeforeEach
    void setUp() {
        coreContext = mock(ApplicationContext.class);
        pluginResource = new PluginResource(coreContext);
    }

    @Test
    void whenAddPluginThenItIsStored() {
        PluginDTO pluginDTO = mock(PluginDTO.class);
        ApplicationContext pluginContext = mock(ApplicationContext.class);
        PluginJar pluginJar = mock(PluginJar.class);
        when(pluginJar.getLibPath()).thenReturn(Path.of("/"));
        //when
        PluginDataLoader pluginData = pluginResource.add(pluginDTO, pluginContext, pluginJar);
        //then
        assertNotNull(pluginData);
        assertEquals(pluginContext, pluginData.context());
    }

    @Test
    void whenRemovePluginThenItIsNoLongerAvailable() {
        PluginDTO pluginDTO = mock(PluginDTO.class);
        pluginResource.remove(pluginDTO);
        assertNull(pluginResource.get(pluginDTO));
    }

    @Test
    void whenLoadAvailableSettingTabsThenReturnsSet() {
        UserSession session = mock(UserSession.class);
        Set<SettingTab> settingTabs = PluginResource.loadAvailableSettingTabs(session);
        assertNotNull(settingTabs);
    }

    @Test
    void whenGetAllWindowConfigThenReturnsSet() {
        Set<WindowConfig> windowConfigs = PluginResource.getAllWindowConfig();
        assertNotNull(windowConfigs);
    }

    @Test
    void whenGetAllVariableProviderThenReturnsSet() {
        Set<VariableProvider> variableProviders = PluginResource.getAllVariableProvider();
        assertNotNull(variableProviders);
    }

    @Test
    void whenLoadRequestMappingForPluginNameThenReturnsOptional() {
        PluginDTO pluginDTO = mock(PluginDTO.class);
        when(pluginDTO.getArtifactName()).thenReturn("test-plugin");
        ApplicationContext context = mock(ApplicationContext.class);
        PluginJar pluginJar = mock(PluginJar.class);
        when(pluginJar.getLibPath()).thenReturn(Path.of("/"));
        //when
        pluginResource.add(pluginDTO, context, pluginJar);
        //then
        Optional<RequestMapping> result = PluginResource.loadRequestMappingForPluginName("test-plugin");
        assertTrue(result.isPresent());
    }
}