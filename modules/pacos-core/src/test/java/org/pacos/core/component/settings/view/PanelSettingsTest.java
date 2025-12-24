package org.pacos.core.component.settings.view;

import org.config.PluginManagerMock;
import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.core.component.settings.view.config.SettingsConfig;
import org.pacos.core.component.settings.view.config.SystemAccessConfig;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PanelSettingsTest {

    @Test
    void whenInitializeThenNoException() {
        VaadinMock.mockSystem();

        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        AutowireCapableBeanFactory beanFactory = Mockito.mock(AutowireCapableBeanFactory.class);
        PluginManagerMock.mockTabResources(Map.of("SystemAccessConfig", new SystemAccessConfig(ProxyMock.registryProxy())), applicationContext);
        when(applicationContext.getAutowireCapableBeanFactory()).thenReturn(beanFactory);

        assertDoesNotThrow(() -> new PanelSettings(new SettingsConfig()));
    }

    @Test
    void whenPluginRemovedThenRefreshTabMap(){
        VaadinMock.mockSystem();
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        AutowireCapableBeanFactory beanFactory = Mockito.mock(AutowireCapableBeanFactory.class);
        PluginManagerMock.mockTabResources(Map.of("SystemAccessConfig", new SystemAccessConfig(ProxyMock.registryProxy())), applicationContext);
        when(applicationContext.getAutowireCapableBeanFactory()).thenReturn(beanFactory);
        PanelSettings panel = new PanelSettings(new SettingsConfig());
        //when
        PluginManagerMock.mockTabResources(new HashMap<>(),applicationContext);
        panel.removeTab();
        //then
        assertTrue(panel.tabMap.isEmpty());
    }

    @Test
    void whenPluginInstalledThenRefreshTabMap(){
        VaadinMock.mockSystem();
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        AutowireCapableBeanFactory beanFactory = Mockito.mock(AutowireCapableBeanFactory.class);
        PluginManagerMock.mockTabResources(new HashMap<>(),applicationContext);
        when(applicationContext.getAutowireCapableBeanFactory()).thenReturn(beanFactory);
        SystemAccessConfig config = new SystemAccessConfig(ProxyMock.registryProxy());
        SystemAccessConfig config2 = new SystemAccessConfig(ProxyMock.registryProxy());
        PluginManagerMock.mockTabResources(Map.of("SystemAccessConfig", config), applicationContext);
        PanelSettings panel = new PanelSettings(new SettingsConfig());
        //when
        PluginManagerMock.mockTabResources(
                Map.of("SystemAccessConfig", config,"System2", config2),
                applicationContext);
        panel.addTab();
        //then
        assertEquals(2,panel.tabMap.size());
    }

}