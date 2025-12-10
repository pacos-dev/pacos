package org.pacos.core.system.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.github.tomakehurst.wiremock.admin.NotFoundException;
import javax.sql.DataSource;
import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.pacos.config.repository.PomDependencyResolver;
import org.pacos.config.repository.RepositoryClient;
import org.pacos.config.repository.data.ModuleConfiguration;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class SystemUpdateServiceTest {

    private RegistryProxy registryProxy;

    private SystemUpdateService systemUpdateService;
    private PluginProxy pluginProxy;

    @BeforeEach
    void setUp() throws SQLException {
        this.pluginProxy = ProxyMock.pluginProxyMock();
        this.registryProxy = pluginProxy.getRegistryProxy();
        DataSource dataSource = mock(DataSource.class);
        this.systemUpdateService = spy(new SystemUpdateService(pluginProxy, dataSource));
        doReturn("1.0").when(registryProxy).readRegistryOrDefault(RegistryName.SYSTEM_VERSION, "1.0");
        when(dataSource.getConnection()).thenReturn(mock(Connection.class));
        when(dataSource.getConnection().createStatement()).thenReturn(mock(Statement.class));
    }

    @Test
    void whenUpdateSystemThenDisableOldPlugins() {
        VaadinMock.mockSystem();
        ModuleConfiguration mockConfig = mock(ModuleConfiguration.class);
        when(mockConfig.version()).thenReturn("1.0.1");
        when(registryProxy.isSystemToUpdate()).thenReturn(true);
        when(registryProxy.readRegistryOrDefault(RegistryName.SYSTEM_VERSION, "1.0")).thenReturn("1.0");
        when(registryProxy.readRegistryOrDefault(RegistryName.AVAILABLE_SYSTEM_VERSION, "")).thenReturn("");

        doAnswer(inv -> null).when(registryProxy).saveRegistry(RegistryName.RESTART_REQUIRED, true);
        try (MockedStatic<RepositoryClient> repositoryMock = mockStatic(RepositoryClient.class)) {
            repositoryMock.when(RepositoryClient::loadModule).thenReturn(mockConfig);

            when(mockConfig.modules()).thenReturn(List.of());

            systemUpdateService.updateSystem(mockConfig);

            verify(registryProxy).saveRegistry(RegistryName.AVAILABLE_SYSTEM_VERSION, "1.0.1");
            verify(registryProxy).saveRegistry(RegistryName.SYSTEM_VERSION, "1.0.1");
            verify(pluginProxy.getPluginService()).disablePluginWithDifferentMajorVersion(new SemanticVersion("1.0.1"));
        }
    }

    @Test
    void whenUpdateSystemButSystemIsNoToUpdateThenSystemIsNotUpdated() {
        ModuleConfiguration mockConfig = mock(ModuleConfiguration.class);
        when(mockConfig.version()).thenReturn("1.0.1");
        when(registryProxy.readRegistryOrDefault(RegistryName.SYSTEM_VERSION, "1.0")).thenReturn("1.0.1");
        try (MockedStatic<RepositoryClient> repositoryMock = mockStatic(RepositoryClient.class)) {
            repositoryMock.when(RepositoryClient::loadModule).thenReturn(mockConfig);

            when(mockConfig.modules()).thenReturn(List.of());

            systemUpdateService.updateSystem(mockConfig);
            verify(registryProxy).readRegistryOrDefault(RegistryName.SYSTEM_VERSION, "1.0");

            verifyNoMoreInteractions(registryProxy);
        }
    }

    @Test
    void whenErrorWhileUpdateSystemThenDoNotOverrideConfiguration() {
        ModuleConfiguration mockConfig = mock(ModuleConfiguration.class);
        when(mockConfig.version()).thenReturn("1.0.1");


        try (MockedStatic<PomDependencyResolver> dependencyMock = mockStatic(PomDependencyResolver.class);
             MockedStatic<RepositoryClient> repositoryClient = mockStatic(RepositoryClient.class)) {
            dependencyMock.when(() -> PomDependencyResolver.resolve(any())).thenThrow(new NotFoundException("artifact not found"));
            repositoryClient.when(RepositoryClient::loadModule).thenReturn(mockConfig);
            when(mockConfig.modules()).thenReturn(List.of());

            assertThrows(NotFoundException.class, () -> systemUpdateService.updateSystem(mockConfig));

            InOrder inOrder = inOrder(registryProxy);
            inOrder.verify(registryProxy).readRegistryOrDefault(RegistryName.SYSTEM_VERSION, "1.0");
            inOrder.verify(registryProxy).saveRegistry(RegistryName.AVAILABLE_SYSTEM_VERSION, "1.0.1");
            inOrder.verify(registryProxy, never()).saveRegistry(RegistryName.SYSTEM_VERSION, "1.0.1");

            verifyNoMoreInteractions(registryProxy);
        }
    }
}
