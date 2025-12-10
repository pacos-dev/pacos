package org.config;

import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.manager.SwaggerUIConfigReload;
import org.pacos.core.component.plugin.service.PluginService;
import org.springframework.context.ApplicationContext;
import org.vaadin.addons.variablefield.provider.VariableProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PluginManagerMock {
    private PluginManagerMock() {
    }

    public static void mockVariableProvider(Map<String, VariableProvider> variableProviderMap) {
        ApplicationContext context = Mockito.mock(ApplicationContext.class);
        when(context.getBeansOfType(VariableProvider.class)).thenReturn(variableProviderMap);
        when(context.getBeansOfType(WindowConfig.class)).thenReturn(Map.of());
        when(context.getBeansOfType(SettingTab.class)).thenReturn(Map.of());
        mockPluginManager(context);
    }

    public static void mockPluginResources(Map<String, WindowConfig> configMap) {
        ApplicationContext context = Mockito.mock(ApplicationContext.class);
        when(context.getBeansOfType(WindowConfig.class)).thenReturn(configMap);
        when(context.getBeansOfType(SettingTab.class)).thenReturn(Map.of());
        mockPluginManager(context);
    }

    public static void mockPluginResources(Map<String, WindowConfig> configMap, ApplicationContext context) {
        when(context.getBeansOfType(WindowConfig.class)).thenReturn(configMap);
        when(context.getBeansOfType(SettingTab.class)).thenReturn(Map.of());
        mockPluginManager(context);
    }

    public static void mockTabResources(Map<String, SettingTab> settingMap, ApplicationContext context) {
        when(context.getBeansOfType(WindowConfig.class)).thenReturn(Map.of());
        when(context.getBeansOfType(SettingTab.class)).thenReturn(settingMap);
        mockPluginManager(context);
    }


    private static void mockPluginManager(ApplicationContext context) {
        PluginService pluginService = mock(PluginService.class);
        when(pluginService.findEnabledPlugin()).thenReturn(List.of());
        when(pluginService.findNotRemovedPlugin()).thenReturn(List.of());
        PluginManager manager = new PluginManager(pluginService, mock(SwaggerUIConfigReload.class), context);
        manager.initializePluginsOnApplicationReadyEvent();
    }

}
