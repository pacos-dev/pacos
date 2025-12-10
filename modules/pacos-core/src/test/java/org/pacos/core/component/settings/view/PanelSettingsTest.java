package org.pacos.core.component.settings.view;

import java.util.Map;

import org.config.PluginManagerMock;
import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.core.component.settings.view.config.SettingsConfig;
import org.pacos.core.component.settings.view.config.SystemAccessConfig;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

}