package org.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

import org.mockito.Mockito;
import org.pacos.core.component.dock.proxy.DockServiceProxy;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.plugin.service.PluginInstallService;
import org.pacos.core.component.plugin.service.PluginService;
import org.pacos.core.component.repository.domain.PluginRepository;
import org.pacos.core.component.repository.service.PluginRepositoryService;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryService;
import org.pacos.core.component.security.service.PermissionService;
import org.pacos.core.component.security.service.RoleService;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.user.service.UserService;
import org.pacos.core.component.variable.proxy.UserVariableCollectionProxy;
import org.pacos.core.component.variable.proxy.UserVariableProxy;
import org.pacos.core.system.proxy.AppProxy;

public class ProxyMock {

    private ProxyMock() {
    }

    public static AppProxy appProxyMock() {
        DockServiceProxy dockServiceProxy = Mockito.mock(DockServiceProxy.class);
        UserVariableCollectionProxy userVariableCollectionProxy = Mockito.mock(UserVariableCollectionProxy.class);
        UserVariableProxy userVariableProxy = Mockito.mock(UserVariableProxy.class);
        UserProxyService userProxyService = Mockito.mock(UserProxyService.class);
        RegistryProxy registryProxy = Mockito.mock(RegistryProxy.class);
        return new AppProxy(dockServiceProxy, userVariableCollectionProxy, userVariableProxy,
                userProxyService, registryProxy, new org.pacos.core.component.plugin.manager.PluginState());
    }

    public static PluginProxy pluginProxyMock() {
        PluginInstallService pluginInstallService = mock(PluginInstallService.class);
        PluginService pluginService = mock(PluginService.class);
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        PluginManager pluginManager = mock(PluginManager.class);
        PluginRepositoryService pluginRepositoryService = mock(PluginRepositoryService.class);
        PluginRepository selectedRepository = mock(PluginRepository.class);
        when(selectedRepository.getId()).thenReturn(1L);
        when(selectedRepository.getName()).thenReturn("Default plugin repository");
        when(selectedRepository.getUrl()).thenReturn(AppRepository.pluginRepo().url());
        when(pluginRepositoryService.findAll()).thenReturn(java.util.List.of(selectedRepository));
        when(pluginRepositoryService.getSelectedRepository()).thenReturn(selectedRepository);
        when(pluginRepositoryService.toAppRepository(selectedRepository)).thenReturn(AppRepository.pluginRepo());
        when(pluginRepositoryService.getSelectedAppRepository()).thenReturn(AppRepository.pluginRepo());
        return new PluginProxy(pluginInstallService, pluginService, registryProxy, pluginManager,
                mock(org.pacos.core.component.plugin.manager.PluginState.class), pluginRepositoryService);
    }

    public static UserProxyService userProxyService() {
        return spy(new UserProxyService(mock(UserService.class),
                mock(RoleService.class),
                mock(RegistryProxy.class),
                mock(PermissionService.class)
        ));
    }

    public static RegistryProxy registryProxy() {
        return spy(new RegistryProxy(Mockito.mock(RegistryService.class)));
    }
}
