package org.pacos.core.component.plugin.event;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.config.repository.DependencyResolverException;
import org.pacos.config.repository.RepositoryClient;
import org.pacos.config.repository.data.ModuleConfiguration;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CheckAvailableSystemUpdateEventTest {

    @Test
    void whenFireEventThenUpdateAvailableVersionAndLastCheckTime() {
        // Arrange
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        ModuleConfiguration expectedModuleConfiguration = mock(ModuleConfiguration.class);
        when(expectedModuleConfiguration.version()).thenReturn("1.0.0");

        try (MockedStatic<RepositoryClient> repositoryClient = mockStatic(RepositoryClient.class)) {
            repositoryClient.when(RepositoryClient::loadModule).thenReturn(expectedModuleConfiguration);

            // when
            ModuleConfiguration result = CheckAvailableSystemUpdateEvent.fireEvent(registryProxy);

            // Assert
            assertEquals(expectedModuleConfiguration, result);
            verify(registryProxy).saveRegistry(RegistryName.AVAILABLE_SYSTEM_VERSION, "1.0.0");
            verify(registryProxy).saveRegistry(eq(RegistryName.LAST_UPDATE_CHECK_TIME), anyLong());
        }
    }

    @Test
    void whenFireEventWithExceptionThenDoNotUpdateAvailableVersion() {
        RegistryProxy registryProxy = mock(RegistryProxy.class);

        try (MockedStatic<RepositoryClient> repositoryClient = mockStatic(RepositoryClient.class)) {
            repositoryClient.when(RepositoryClient::loadModule).thenThrow(DependencyResolverException.class);

            // when
            assertThrows(DependencyResolverException.class, () -> CheckAvailableSystemUpdateEvent.fireEvent(registryProxy));

            // Assert
            verifyNoInteractions(registryProxy);
        }
    }
}