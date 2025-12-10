package org.pacos.core.system.service;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.config.repository.data.ModuleConfiguration;
import org.pacos.core.component.plugin.event.CheckAvailableSystemUpdateEvent;
import org.pacos.core.component.plugin.service.PluginUpdateService;
import org.pacos.core.component.plugin.service.PluginVersionChecker;
import org.pacos.core.component.plugin.service.data.PluginsToUpdate;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.system.service.data.SystemUpdateResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutoUpdateServiceTest {

    private AutoUpdateService autoUpdateService;
    private SystemUpdateService systemUpdateService;
    private PluginVersionChecker pluginVersionChecker;
    private RegistryProxy registryProxy;
    private PluginUpdateService pluginUpdateService;

    @BeforeEach
    void setUp() {
        systemUpdateService = mock(SystemUpdateService.class);
        pluginVersionChecker = mock(PluginVersionChecker.class);
        registryProxy = mock(RegistryProxy.class);
        pluginUpdateService = mock(PluginUpdateService.class);
        autoUpdateService = new AutoUpdateService(systemUpdateService, pluginVersionChecker, registryProxy, pluginUpdateService);
    }

    @Test
    void whenUpdateSystemAndAutoUpdateEnabledThenReturnSystemUpdateResult() {
        // Arrange
        ModuleConfiguration moduleConfiguration = mock(ModuleConfiguration.class);
        SystemUpdateResult updateResult = new SystemUpdateResult("1.0", "1.1");

        when(registryProxy.readBoolean(RegistryName.AUTO_UPDATE_ENABLED, false)).thenReturn(true);
        when(systemUpdateService.updateSystem(moduleConfiguration)).thenReturn(updateResult);
        when(moduleConfiguration.version()).thenReturn("1.0.0");

        try (MockedStatic<CheckAvailableSystemUpdateEvent> mockedCheckEvent = mockStatic(CheckAvailableSystemUpdateEvent.class)) {
            mockedCheckEvent.when(() -> CheckAvailableSystemUpdateEvent.fireEvent(registryProxy)).thenReturn(moduleConfiguration);

            // when
            Optional<SystemUpdateResult> result = autoUpdateService.updateSystem(false);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(updateResult, result.get());
        }
    }

    @Test
    void whenUpdateSystemAndAutoUpdateDisabledThenReturnEmpty() {
        // Arrange
        when(pluginVersionChecker.checkPluginsToUpdate()).thenReturn(mock(PluginsToUpdate.class));
        when(registryProxy.readBoolean(RegistryName.AUTO_UPDATE_ENABLED, false)).thenReturn(false);

        try (MockedStatic<CheckAvailableSystemUpdateEvent> mockedCheckEvent = mockStatic(CheckAvailableSystemUpdateEvent.class)) {
            mockedCheckEvent.when(() -> CheckAvailableSystemUpdateEvent.fireEvent(registryProxy)).thenReturn(mock(ModuleConfiguration.class));

            // when
            Optional<SystemUpdateResult> result = autoUpdateService.updateSystem(false);

            // Assert
            assertFalse(result.isPresent());
        }
    }

    @Test
    void whenCheckIfUpdateAvailableThenReturnTrueIfUpdatesExist() {
        // Arrange
        PluginsToUpdate pluginsToUpdate = mock(PluginsToUpdate.class);
        when(pluginsToUpdate.plugins()).thenReturn(Collections.emptyList());
        when(pluginVersionChecker.checkPluginsToUpdate()).thenReturn(pluginsToUpdate);
        when(registryProxy.isSystemToUpdate()).thenReturn(true);

        try (MockedStatic<CheckAvailableSystemUpdateEvent> mockedCheckEvent = mockStatic(CheckAvailableSystemUpdateEvent.class)) {
            mockedCheckEvent.when(() -> CheckAvailableSystemUpdateEvent.fireEvent(registryProxy)).thenReturn(mock(ModuleConfiguration.class));

            // when
            boolean result = autoUpdateService.checkIfUpdateAvailable();

            // Assert
            assertTrue(result);
        }
    }

    @Test
    void whenCheckIfUpdateAvailableThenReturnFalseIfNoUpdatesExist() {
        // Arrange
        PluginsToUpdate pluginsToUpdate = mock(PluginsToUpdate.class);
        when(pluginsToUpdate.plugins()).thenReturn(Collections.emptyList());
        when(pluginVersionChecker.checkPluginsToUpdate()).thenReturn(pluginsToUpdate);
        when(registryProxy.isSystemToUpdate()).thenReturn(false);

        try (MockedStatic<CheckAvailableSystemUpdateEvent> mockedCheckEvent = mockStatic(CheckAvailableSystemUpdateEvent.class)) {
            mockedCheckEvent.when(() -> CheckAvailableSystemUpdateEvent.fireEvent(registryProxy)).thenReturn(mock(ModuleConfiguration.class));

            // when
            boolean result = autoUpdateService.checkIfUpdateAvailable();

            // Assert
            assertFalse(result);
        }
    }

    @Test
    void whenUpdateSystemWithValidInputsThenReturnUpdatedSystemResult() {
        // Arrange
        ModuleConfiguration systemConfiguration = mock(ModuleConfiguration.class);
        SystemUpdateResult result = new SystemUpdateResult("1.0", "1.1");

        when(systemUpdateService.updateSystem(systemConfiguration)).thenReturn(result);
        when(systemConfiguration.version()).thenReturn("1.0.0");
        // Act
        autoUpdateService.updateSystem(systemConfiguration);

        // Assert
        verify(registryProxy).saveRegistry(eq(RegistryName.RESTART_REQUIRED), eq(true));
    }

    @Test
    void whenUpdateSystemWithNoUpdatesThenReturnEmptyOptional() {
        // Arrange
        ModuleConfiguration systemConfiguration = mock(ModuleConfiguration.class);
        SystemUpdateResult versionUpdateResult = mock(SystemUpdateResult.class);

        when(systemUpdateService.updateSystem(systemConfiguration)).thenReturn(versionUpdateResult);
        when(systemConfiguration.version()).thenReturn("1.0.0");

        // Act
        autoUpdateService.updateSystem(systemConfiguration);

        // Assert
        verify(registryProxy, never()).saveRegistry(RegistryName.RESTART_REQUIRED, true);
        verify(registryProxy, never()).saveRegistry(RegistryName.LAST_UPDATE_TIME, System.currentTimeMillis());
    }
}
