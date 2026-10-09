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
import org.pacos.base.listener.PluginListener;
import org.pacos.core.component.plugin.manager.data.RequestMapping;
import org.springframework.context.ApplicationContext;
import org.vaadin.addons.variablefield.provider.VariableProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PluginResourceTest {

    private ApplicationContext coreContext;
    private PluginResource pluginResource;

    @BeforeEach
    void setUp() {
        coreContext = mock(ApplicationContext.class);
        when(coreContext.getBeansOfType(PluginListener.class)).thenReturn(java.util.Collections.emptyMap());
        when(coreContext.getBeansOfType(WindowConfig.class)).thenReturn(java.util.Collections.emptyMap());
        when(coreContext.getBeansOfType(SettingTab.class)).thenReturn(java.util.Collections.emptyMap());
        when(coreContext.getBeansOfType(VariableProvider.class)).thenReturn(java.util.Collections.emptyMap());
        when(coreContext.getBeansOfType(com.vaadin.flow.server.RequestHandler.class)).thenReturn(java.util.Collections.emptyMap());
        pluginResource = new PluginResource(coreContext);
    }

    @Test
    void whenAddPluginThenItIsStored() {
        PluginDTO pluginDTO = mock(PluginDTO.class);
        ApplicationContext pluginContext = mock(ApplicationContext.class);
        PluginJar pluginJar = mock(PluginJar.class);
        when(pluginJar.getLibPath()).thenReturn(Path.of("/"));
        when(pluginContext.getBeansOfType(PluginListener.class)).thenReturn(java.util.Collections.emptyMap());
        when(pluginContext.getBeansOfType(WindowConfig.class)).thenReturn(java.util.Collections.emptyMap());
        when(pluginContext.getBeansOfType(SettingTab.class)).thenReturn(java.util.Collections.emptyMap());
        when(pluginContext.getBeansOfType(VariableProvider.class)).thenReturn(java.util.Collections.emptyMap());
        when(pluginContext.getBeansOfType(com.vaadin.flow.server.RequestHandler.class)).thenReturn(java.util.Collections.emptyMap());

        PluginDataLoader pluginData = pluginResource.add(pluginDTO, pluginContext, pluginJar);

        assertNotNull(pluginData);
        assertEquals(pluginContext, pluginData.context());
        assertEquals(pluginData, pluginResource.get(pluginDTO));
    }

    @Test
    void whenRemoveMissingPluginThenNoExceptionIsThrown() {
        PluginDTO pluginDTO = mock(PluginDTO.class);

        pluginResource.remove(pluginDTO);

        assertNull(pluginResource.get(pluginDTO));
    }

    @Test
    void whenRemovePluginThenItIsNoLongerAvailableAndListenersAreNotified() {
        PluginDTO pluginDTO = mock(PluginDTO.class);
        ApplicationContext pluginContext = mock(ApplicationContext.class);
        PluginJar pluginJar = mock(PluginJar.class);
        PluginListener listener = mock(PluginListener.class);
        when(pluginJar.getLibPath()).thenReturn(Path.of("/"));
        when(coreContext.getBeansOfType(PluginListener.class)).thenReturn(java.util.Map.of("listener", listener));
        when(pluginContext.getBeansOfType(PluginListener.class)).thenReturn(java.util.Collections.emptyMap());
        when(pluginContext.getBeansOfType(WindowConfig.class)).thenReturn(java.util.Collections.emptyMap());
        when(pluginContext.getBeansOfType(SettingTab.class)).thenReturn(java.util.Collections.emptyMap());
        when(pluginContext.getBeansOfType(VariableProvider.class)).thenReturn(java.util.Collections.emptyMap());
        when(pluginContext.getBeansOfType(com.vaadin.flow.server.RequestHandler.class)).thenReturn(java.util.Collections.emptyMap());
        pluginResource = new PluginResource(coreContext);
        pluginResource.add(pluginDTO, pluginContext, pluginJar);

        pluginResource.remove(pluginDTO);

        assertNull(pluginResource.get(pluginDTO));
        verify(listener).pluginRemoved(pluginContext);
    }

    @Test
    void whenRemovePluginAndPluginListenerFailsThenRemovalStillCompletes() {
        PluginDTO pluginDTO = mock(PluginDTO.class);
        ApplicationContext pluginContext = mock(ApplicationContext.class);
        PluginJar pluginJar = mock(PluginJar.class);
        PluginListener listener = mock(PluginListener.class);
        when(pluginJar.getLibPath()).thenReturn(Path.of("/"));
        when(coreContext.getBeansOfType(PluginListener.class)).thenReturn(java.util.Map.of("listener", listener));
        when(pluginContext.getBeansOfType(PluginListener.class)).thenReturn(java.util.Collections.emptyMap());
        when(pluginContext.getBeansOfType(WindowConfig.class)).thenReturn(java.util.Collections.emptyMap());
        when(pluginContext.getBeansOfType(SettingTab.class)).thenReturn(java.util.Collections.emptyMap());
        when(pluginContext.getBeansOfType(VariableProvider.class)).thenReturn(java.util.Collections.emptyMap());
        when(pluginContext.getBeansOfType(com.vaadin.flow.server.RequestHandler.class)).thenReturn(java.util.Collections.emptyMap());
        doThrow(new IllegalStateException()).when(listener).pluginRemoved(pluginContext);
        pluginResource = new PluginResource(coreContext);
        pluginResource.add(pluginDTO, pluginContext, pluginJar);

        pluginResource.remove(pluginDTO);

        assertNull(pluginResource.get(pluginDTO));
        verify(listener).pluginRemoved(pluginContext);
    }

    @Test
    void whenLoadAvailableSettingTabsThenReturnsSet() {
        UserSession session = mock(UserSession.class);
        Set<SettingTab> settingTabs = PluginResource.loadAvailableSettingTabs(session);
        assertNotNull(settingTabs);
    }

    @Test
    void whenGetAllWindowConfigThenReturnsSet() {
        assertNotNull(PluginResource.getAllWindowConfig());
    }

    @Test
    void whenGetAllVariableProviderThenReturnsSet() {
        assertNotNull(PluginResource.getAllVariableProvider());
    }

    @Test
    void whenLoadRequestMappingForExistingPluginThenReturnsOptional() {
        PluginDTO pluginDTO = mock(PluginDTO.class);
        when(pluginDTO.getArtifactName()).thenReturn("test-plugin");
        ApplicationContext context = mock(ApplicationContext.class);
        PluginJar pluginJar = mock(PluginJar.class);
        when(pluginJar.getLibPath()).thenReturn(Path.of("/"));
        when(context.getBeansOfType(PluginListener.class)).thenReturn(java.util.Collections.emptyMap());
        when(context.getBeansOfType(WindowConfig.class)).thenReturn(java.util.Collections.emptyMap());
        when(context.getBeansOfType(SettingTab.class)).thenReturn(java.util.Collections.emptyMap());
        when(context.getBeansOfType(VariableProvider.class)).thenReturn(java.util.Collections.emptyMap());
        when(context.getBeansOfType(com.vaadin.flow.server.RequestHandler.class)).thenReturn(java.util.Collections.emptyMap());

        pluginResource.add(pluginDTO, context, pluginJar);

        Optional<RequestMapping> result = PluginResource.loadRequestMappingForPluginName("test-plugin");
        assertTrue(result.isPresent());
    }

    @Test
    void whenLoadRequestMappingForUnknownPluginThenReturnsEmptyOptional() {
        assertTrue(PluginResource.loadRequestMappingForPluginName("unknown").isEmpty());
    }
}
