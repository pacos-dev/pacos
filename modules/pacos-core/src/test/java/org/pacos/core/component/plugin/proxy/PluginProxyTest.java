package org.pacos.core.component.plugin.proxy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.service.PluginInstallService;
import org.pacos.core.component.plugin.service.PluginService;
import org.pacos.core.component.registry.proxy.RegistryProxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PluginProxyTest {

    private PluginInstallService pluginInstallServiceMock;
    private PluginService pluginServiceMock;
    private PluginProxy pluginProxy;
    private RegistryProxy registryProxyMock;
    private PluginManager pluginManager;

    @BeforeEach
    void setUp() {
        pluginInstallServiceMock = mock(PluginInstallService.class);
        pluginServiceMock = mock(PluginService.class);
        registryProxyMock = mock(RegistryProxy.class);
        pluginProxy = new PluginProxy(pluginInstallServiceMock, pluginServiceMock, registryProxyMock, pluginManager);
    }

    @Test
    void whenCalledGetPluginInstallServiceThenReturnCorrectInstance() {
        assertEquals(pluginInstallServiceMock, pluginProxy.getPluginInstallService());
    }

    @Test
    void whenCalledGetPluginServiceThenReturnCorrectInstance() {
        assertEquals(pluginServiceMock, pluginProxy.getPluginService());
    }

    @Test
    void whenCalledGetRegistryProxyThenReturnCorrectInstance() {
        assertEquals(registryProxyMock, pluginProxy.getRegistryProxy());
    }

    @Test
    void whenCalledGetPluginManagerThenReturnCorrectInstance() {
        assertEquals(pluginManager, pluginProxy.getPluginManager());
    }

}