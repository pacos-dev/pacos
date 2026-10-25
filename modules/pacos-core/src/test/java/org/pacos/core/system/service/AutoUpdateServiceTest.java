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
        when(registryProxy.readRegistryOrDefault(RegistryName.SYSTEM_VERSION, "1.0")).thenReturn("1.0.0");
        autoUpdateService = new AutoUpdateService(systemUpdateService, pluginVersionChecker, registryProxy, pluginUpdateService);
    }

    @Test
    void whenUpdateSystemAndAutoUpdateEnabledThenReturnSystemUpdateResult() {
        ModuleConfiguration moduleConfiguration = mock(ModuleConfiguration.class);
        SystemUpdateResult updateResult = new SystemUpdateResult("1.0", "1.1");

        when(registryProxy.readBoolean(RegistryName.AUTO_UPDATE_ENABLED, false)).thenReturn(true);
        when(systemUpdateService.updateSystem(moduleConfiguration)).thenReturn(updateResult);
        when(moduleConfiguration.version()).thenReturn("1.0.0");

        try (MockedStatic<CheckAvailableSystemUpdateEvent> mockedCheckEvent = mockStatic(CheckAvailableSystemUpdateEvent.class)) {
            mockedCheckEvent.when(() -> CheckAvailableSystemUpdateEvent.fireEvent(registryProxy)).thenReturn(moduleConfiguration);

            Optional<SystemUpdateResult> result = autoUpdateService.updateSystem(false);

            assertTrue(result.isPresent());
            assertEquals(updateResult, result.get());
        }
    }

    @Test
    void whenAutomaticUpdateWouldChangeMajorVersionThenSkipSystemUpdate() {
        ModuleConfiguration moduleConfiguration = mock(ModuleConfiguration.class);
        when(moduleConfiguration.version()).thenReturn("4.0.0");
        when(registryProxy.readBoolean(RegistryName.AUTO_UPDATE_ENABLED, false)).thenReturn(true);
        when(registryProxy.readRegistryOrDefault(RegistryName.SYSTEM_VERSION, "1.0")).thenReturn("3.9.9");

        try (MockedStatic<CheckAvailableSystemUpdateEvent> mockedCheckEvent = mockStatic(CheckAvailableSystemUpdateEvent.class)) {
            mockedCheckEvent.when(() -> CheckAvailableSystemUpdateEvent.fireEvent(registryProxy)).thenReturn(moduleConfiguration);

            Optional<SystemUpdateResult> result = autoUpdateService.updateSystem(false);

            assertFalse(result.isPresent());
            verify(systemUpdateService, never()).updateSystem(moduleConfiguration);
        }
    }

    @Test
    void whenManualUpdateWouldChangeMajorVersionThenAllowSystemUpdate() {
        ModuleConfiguration moduleConfiguration = mock(ModuleConfiguration.class);
        SystemUpdateResult updateResult = new SystemUpdateResult("3.9.9", "4.0.0");
        when(moduleConfiguration.version()).thenReturn("4.0.0");
        when(registryProxy.readBoolean(RegistryName.AUTO_UPDATE_ENABLED, false)).thenReturn(false);
        when(registryProxy.readRegistryOrDefault(RegistryName.SYSTEM_VERSION, "1.0")).thenReturn("3.9.9");
        when(systemUpdateService.updateSystem(moduleConfiguration)).thenReturn(updateResult);

        try (MockedStatic<CheckAvailableSystemUpdateEvent> mockedCheckEvent = mockStatic(CheckAvailableSystemUpdateEvent.class)) {
            mockedCheckEvent.when(() -> CheckAvailableSystemUpdateEvent.fireEvent(registryProxy)).thenReturn(moduleConfiguration);

            Optional<SystemUpdateResult> result = autoUpdateService.updateSystem(true);

            assertTrue(result.isPresent());
            assertEquals(updateResult, result.get());
            verify(systemUpdateService).updateSystem(moduleConfiguration);
        }
    }

    @Test
    void whenAutomaticUpdateChangesTwoDigitMinorVersionThenAllowSystemUpdate() {
        ModuleConfiguration moduleConfiguration = mock(ModuleConfiguration.class);
        SystemUpdateResult updateResult = new SystemUpdateResult("3.9.9", "3.10.0");
        when(moduleConfiguration.version()).thenReturn("3.10.0");
        when(registryProxy.readBoolean(RegistryName.AUTO_UPDATE_ENABLED, false)).thenReturn(true);
        when(registryProxy.readRegistryOrDefault(RegistryName.SYSTEM_VERSION, "1.0")).thenReturn("3.9.9");
        when(systemUpdateService.updateSystem(moduleConfiguration)).thenReturn(updateResult);

        try (MockedStatic<CheckAvailableSystemUpdateEvent> mockedCheckEvent = mockStatic(CheckAvailableSystemUpdateEvent.class)) {
            mockedCheckEvent.when(() -> CheckAvailableSystemUpdateEvent.fireEvent(registryProxy)).thenReturn(moduleConfiguration);

            Optional<SystemUpdateResult> result = autoUpdateService.updateSystem(false);

            assertTrue(result.isPresent());
            assertEquals(updateResult, result.get());
            verify(systemUpdateService).updateSystem(moduleConfiguration);
        }
    }

    @Test
    void whenUpdateSystemAndAutoUpdateDisabledThenReturnEmpty() {
        when(registryProxy.readBoolean(RegistryName.AUTO_UPDATE_ENABLED, false)).thenReturn(false);

        try (MockedStatic<CheckAvailableSystemUpdateEvent> mockedCheckEvent = mockStatic(CheckAvailableSystemUpdateEvent.class)) {
            mockedCheckEvent.when(() -> CheckAvailableSystemUpdateEvent.fireEvent(registryProxy)).thenReturn(mock(ModuleConfiguration.class));

            Optional<SystemUpdateResult> result = autoUpdateService.updateSystem(false);

            assertFalse(result.isPresent());
        }
    }

    @Test
    void whenCheckIfUpdateAvailableThenReturnTrueIfUpdatesExist() {
        PluginsToUpdate pluginsToUpdate = mock(PluginsToUpdate.class);
        when(pluginsToUpdate.plugins()).thenReturn(Collections.emptyList());
        when(pluginVersionChecker.checkPluginsToUpdate()).thenReturn(pluginsToUpdate);
        when(registryProxy.isSystemToUpdate()).thenReturn(true);

        try (MockedStatic<CheckAvailableSystemUpdateEvent> mockedCheckEvent = mockStatic(CheckAvailableSystemUpdateEvent.class)) {
            mockedCheckEvent.when(() -> CheckAvailableSystemUpdateEvent.fireEvent(registryProxy)).thenReturn(mock(ModuleConfiguration.class));

            boolean result = autoUpdateService.checkIfUpdateAvailable();

            assertTrue(result);
        }
    }

    @Test
    void whenCheckIfUpdateAvailableThenReturnFalseIfNoUpdatesExist() {
        PluginsToUpdate pluginsToUpdate = mock(PluginsToUpdate.class);
        when(pluginsToUpdate.plugins()).thenReturn(Collections.emptyList());
        when(pluginVersionChecker.checkPluginsToUpdate()).thenReturn(pluginsToUpdate);
        when(registryProxy.isSystemToUpdate()).thenReturn(false);

        try (MockedStatic<CheckAvailableSystemUpdateEvent> mockedCheckEvent = mockStatic(CheckAvailableSystemUpdateEvent.class)) {
            mockedCheckEvent.when(() -> CheckAvailableSystemUpdateEvent.fireEvent(registryProxy)).thenReturn(mock(ModuleConfiguration.class));

            boolean result = autoUpdateService.checkIfUpdateAvailable();

            assertFalse(result);
        }
    }

    @Test
    void whenUpdateSystemWithValidInputsThenReturnUpdatedSystemResult() {
        ModuleConfiguration systemConfiguration = mock(ModuleConfiguration.class);
        SystemUpdateResult result = new SystemUpdateResult("1.0", "1.1");

        when(systemUpdateService.updateSystem(systemConfiguration)).thenReturn(result);
        when(systemConfiguration.version()).thenReturn("1.0.0");

        autoUpdateService.updateSystem(systemConfiguration);

        verify(registryProxy).saveRegistry(RegistryName.RESTART_REQUIRED, true);
    }

    @Test
    void whenUpdateSystemWithNoUpdatesThenReturnEmptyOptional() {
        ModuleConfiguration systemConfiguration = mock(ModuleConfiguration.class);
        SystemUpdateResult versionUpdateResult = mock(SystemUpdateResult.class);

        when(systemUpdateService.updateSystem(systemConfiguration)).thenReturn(versionUpdateResult);
        when(systemConfiguration.version()).thenReturn("1.0.0");

        autoUpdateService.updateSystem(systemConfiguration);

        verify(registryProxy, never()).saveRegistry(RegistryName.RESTART_REQUIRED, true);
        verify(registryProxy, never()).saveRegistry(RegistryName.LAST_UPDATE_TIME, System.currentTimeMillis());
    }
}
