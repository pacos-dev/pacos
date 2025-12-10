package org.pacos.core.component.plugin.manager.data;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import com.vaadin.flow.server.RequestHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.window.config.WindowConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.vaadin.addons.variablefield.provider.VariableProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PluginDataLoaderTest {

    private ApplicationContext context;
    private PluginJar pluginJar;
    private PluginDataLoader pluginData;

    @BeforeEach
    void setUp() {
        context = mock(ApplicationContext.class);
        pluginJar = mock(PluginJar.class);
        when(pluginJar.getLibPath()).thenReturn(Path.of("/"));
        when(context.getBeansOfType(WindowConfig.class)).thenReturn(Collections.emptyMap());
        when(context.getBeansOfType(SettingTab.class)).thenReturn(Collections.emptyMap());
        when(context.getBeansOfType(VariableProvider.class)).thenReturn(Collections.emptyMap());
        when(context.getBeansOfType(RequestHandler.class)).thenReturn(Collections.emptyMap());
        when(context.getBean(RequestMappingInfoHandlerMapping.class)).thenReturn(mock(RequestMappingInfoHandlerMapping.class));
        when(context.getBean(RequestMappingHandlerAdapter.class)).thenReturn(mock(RequestMappingHandlerAdapter.class));
        pluginData = new PluginDataLoader(context, pluginJar);
    }

    @Test
    void whenCreatePluginDataThenContextIsSet() {
        assertEquals(context, pluginData.context());
    }

    @Test
    void whenCreatePluginDataThenCollectionsAreInitialized() {
        assertNotNull(pluginData.getWindowConfigSet());
        assertNotNull(pluginData.getSettingsTab());
        assertNotNull(pluginData.getVariableProviders());
        assertNotNull(pluginData.getRequestHandlers());
    }

    @Test
    void whenGetRequestMappingThenReturnRequestMapping() {
        assertNotNull(pluginData.getRequestMapping());
        assertInstanceOf(RequestMapping.class, pluginData.getRequestMapping());
    }

    @Test
    void whenSetRequestHandlerRegistrationThenItIsStored() {
        Set<RequestHandlerRegistration> registrations = Collections.emptySet();
        pluginData.setRequestHandlerRegistration(registrations);
        assertEquals(registrations, pluginData.getRequestHandlerRegistration());
    }

    @Test
    void whenCloseThenPluginJarAndContextAreClosed() {
        ConfigurableApplicationContext configurableContext = mock(ConfigurableApplicationContext.class);
        PluginDataLoader pluginDataWithConfigurableContext = new PluginDataLoader(configurableContext, pluginJar);
        pluginDataWithConfigurableContext.close();
        verify(pluginJar).closeClassLoader();
        verify(configurableContext).close();
    }
}