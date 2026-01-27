package org.pacos.core.component.settings.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.config.PluginManagerMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.core.component.settings.view.config.SettingsConfig;
import org.pacos.core.component.settings.view.config.SystemAccessConfig;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

class PanelSettingsTest {

    @Test
    void whenInitializeThenNoException() {
        VaadinMock.mockSystem();

        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        AutowireCapableBeanFactory beanFactory = Mockito.mock(AutowireCapableBeanFactory.class);
        PluginManagerMock.mockTabResources(Map.of("SystemAccessConfig", new SystemAccessConfig(applicationContext)), applicationContext);
        when(applicationContext.getAutowireCapableBeanFactory()).thenReturn(beanFactory);

        assertDoesNotThrow(() -> new PanelSettings(new SettingsConfig()));
    }

    @Test
    void whenPluginRemovedThenRefreshTabMap(){
        VaadinMock.mockSystem();
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        AutowireCapableBeanFactory beanFactory = Mockito.mock(AutowireCapableBeanFactory.class);
        PluginManagerMock.mockTabResources(Map.of("SystemAccessConfig", new SystemAccessConfig(applicationContext)), applicationContext);
        when(applicationContext.getAutowireCapableBeanFactory()).thenReturn(beanFactory);
        PanelSettings panel = new PanelSettings(new SettingsConfig());
        //when
        PluginManagerMock.mockTabResources(new HashMap<>(),applicationContext);
        panel.removeTab();
        //then
        assertEquals(0,panel.getMenuGrid().getDataProvider()
                .getChildCount(new HierarchicalQuery<>(null,null)));
    }

    @Test
    void whenPluginInstalledThenRefreshTabMap(){
        VaadinMock.mockSystem();
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        AutowireCapableBeanFactory beanFactory = Mockito.mock(AutowireCapableBeanFactory.class);
        PluginManagerMock.mockTabResources(new HashMap<>(),applicationContext);
        when(applicationContext.getAutowireCapableBeanFactory()).thenReturn(beanFactory);
        SystemAccessConfig config = new SystemAccessConfig(applicationContext) {
            @Override
            public String[] getGroup() {
                return null;
            }
        };
        SystemAccessConfig config2 = new SystemAccessConfig(applicationContext) {
            @Override
            public String[] getGroup() {
                return null;
            }
        };
        PluginManagerMock.mockTabResources(Map.of("SystemAccessConfig", config), applicationContext);
        PanelSettings panel = new PanelSettings(new SettingsConfig());
        //when
        PluginManagerMock.mockTabResources(
                Map.of("SystemAccessConfig", config,"System2", config2),
                applicationContext);
        panel.reloadSettingMenu();
        //then
        assertEquals(2,panel.getMenuGrid().getDataProvider()
                .getChildCount(new HierarchicalQuery<>(null,null)));

    }

}