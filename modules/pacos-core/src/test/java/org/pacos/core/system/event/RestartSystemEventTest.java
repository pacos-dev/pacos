package org.pacos.core.system.event;

import java.rmi.RemoteException;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.config.process.StarterRecoveryService;
import org.pacos.config.rmi.RemoteRegistryService;
import org.pacos.config.rmi.RemoteRestartCInterface;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RestartSystemEventTest {

    @Test
    void whenFireEventThenReturnTrue() throws RemoteException {
        try (MockedStatic<RemoteRegistryService> mockedRemoteRegistryService = mockStatic(RemoteRegistryService.class)) {
            RemoteRestartCInterface remoteInterface = mock(RemoteRestartCInterface.class);
            when(remoteInterface.isAlive()).thenReturn(true);
            mockedRemoteRegistryService.when(RemoteRegistryService::loadRemoteInterface).thenReturn(remoteInterface);

            boolean result = RestartSystemEvent.fireEvent();

            assertTrue(result);
            verify(remoteInterface).isAlive();
            verify(remoteInterface).restart();
        }
    }

    @Test
    void whenFireEventAndStarterIsUnavailableThenRecoverBeforeRestart() throws RemoteException {
        RemoteRestartCInterface unavailableInterface = mock(RemoteRestartCInterface.class);
        RemoteRestartCInterface recoveredInterface = mock(RemoteRestartCInterface.class);
        when(unavailableInterface.isAlive()).thenThrow(new RemoteException("Starter is down"));
        when(recoveredInterface.isAlive()).thenReturn(true);

        try (MockedStatic<RemoteRegistryService> mockedRemoteRegistryService = mockStatic(RemoteRegistryService.class);
             MockedStatic<StarterRecoveryService> mockedStarterRecovery = mockStatic(StarterRecoveryService.class)) {
            mockedRemoteRegistryService.when(RemoteRegistryService::loadRemoteInterface)
                    .thenReturn(unavailableInterface, recoveredInterface);
            mockedStarterRecovery.when(StarterRecoveryService::startInAttachMode).thenReturn(true);

            boolean result = RestartSystemEvent.fireEvent();

            assertTrue(result);
            mockedStarterRecovery.verify(StarterRecoveryService::startInAttachMode);
            verify(unavailableInterface, never()).restart();
            verify(recoveredInterface).isAlive();
            verify(recoveredInterface).restart();
        }
    }

    @Test
    void whenAttachRecoveryTimesOutThenStartStarterInRecoveryMode() throws Exception {
        RemoteRestartCInterface unavailableInterface = mock(RemoteRestartCInterface.class);
        RemoteRestartCInterface recoveredInterface = mock(RemoteRestartCInterface.class);
        when(unavailableInterface.isAlive()).thenThrow(new RemoteException("Starter is down"));
        when(recoveredInterface.isAlive()).thenReturn(true);

        try (MockedStatic<RemoteRegistryService> mockedRemoteRegistryService = mockStatic(RemoteRegistryService.class);
             MockedStatic<StarterRecoveryService> mockedStarterRecovery = mockStatic(StarterRecoveryService.class)) {
            mockedRemoteRegistryService.when(RemoteRegistryService::loadRemoteInterface)
                    .thenThrow(new RuntimeException("RMI registry unavailable"))
                    .thenReturn(unavailableInterface, recoveredInterface);
            mockedStarterRecovery.when(StarterRecoveryService::startInAttachMode).thenReturn(true);
            mockedStarterRecovery.when(StarterRecoveryService::startInRecoveryMode).thenReturn(true);

            RemoteRestartCInterface result = RestartSystemEvent.loadOrRecoverStarter(0, 1000, 1);

            assertSame(recoveredInterface, result);
            mockedStarterRecovery.verify(StarterRecoveryService::startInAttachMode);
            mockedStarterRecovery.verify(StarterRecoveryService::startInRecoveryMode);
            verify(recoveredInterface).isAlive();
        }
    }

    @Test
    void whenAttachProcessCannotBeLaunchedThenUseRecoveryMode() throws Exception {
        RemoteRestartCInterface recoveredInterface = mock(RemoteRestartCInterface.class);
        when(recoveredInterface.isAlive()).thenReturn(true);

        try (MockedStatic<RemoteRegistryService> mockedRemoteRegistryService = mockStatic(RemoteRegistryService.class);
             MockedStatic<StarterRecoveryService> mockedStarterRecovery = mockStatic(StarterRecoveryService.class)) {
            mockedRemoteRegistryService.when(RemoteRegistryService::loadRemoteInterface)
                    .thenThrow(new RemoteException("Starter RMI is unavailable"))
                    .thenReturn(recoveredInterface);
            mockedStarterRecovery.when(StarterRecoveryService::startInAttachMode).thenReturn(false);
            mockedStarterRecovery.when(StarterRecoveryService::startInRecoveryMode).thenReturn(true);

            RemoteRestartCInterface result = RestartSystemEvent.loadOrRecoverStarter(0, 1000, 1);

            assertSame(recoveredInterface, result);
            mockedStarterRecovery.verify(StarterRecoveryService::startInAttachMode);
            mockedStarterRecovery.verify(StarterRecoveryService::startInRecoveryMode);
            verify(recoveredInterface).isAlive();
        }
    }

    @Test
    void whenBothRecoveryAttemptsFailThenDoNotReturnAnUnhealthyEndpoint() {
        try (MockedStatic<RemoteRegistryService> mockedRemoteRegistryService = mockStatic(RemoteRegistryService.class);
             MockedStatic<StarterRecoveryService> mockedStarterRecovery = mockStatic(StarterRecoveryService.class)) {
            mockedRemoteRegistryService.when(RemoteRegistryService::loadRemoteInterface)
                    .thenThrow(new RemoteException("Starter RMI is unavailable"));
            mockedStarterRecovery.when(StarterRecoveryService::startInAttachMode).thenReturn(true);
            mockedStarterRecovery.when(StarterRecoveryService::startInRecoveryMode).thenReturn(true);

            assertThrows(Exception.class,
                    () -> RestartSystemEvent.loadOrRecoverStarter(0, 0, 1));
            mockedStarterRecovery.verify(StarterRecoveryService::startInAttachMode);
            mockedStarterRecovery.verify(StarterRecoveryService::startInRecoveryMode);
        }
    }

    @Test
    void whenStarterCannotBeStartedThenReturnFalse() {
        try (MockedStatic<RemoteRegistryService> mockedRemoteRegistryService = mockStatic(RemoteRegistryService.class);
             MockedStatic<StarterRecoveryService> mockedStarterRecovery = mockStatic(StarterRecoveryService.class);
             MockedStatic<NotificationUtils> mockedNotificationUtils = mockStatic(NotificationUtils.class)) {
            mockedRemoteRegistryService.when(RemoteRegistryService::loadRemoteInterface)
                    .thenThrow(new RuntimeException("Mocked Exception"));
            mockedStarterRecovery.when(StarterRecoveryService::startInAttachMode).thenReturn(false);
            mockedStarterRecovery.when(StarterRecoveryService::startInRecoveryMode).thenReturn(false);

            boolean result = RestartSystemEvent.fireEvent();

            assertFalse(result);
            mockedStarterRecovery.verify(StarterRecoveryService::startInAttachMode);
            mockedStarterRecovery.verify(StarterRecoveryService::startInRecoveryMode);
            mockedNotificationUtils.verify(() -> NotificationUtils.error(
                    "Looks like the PacOSRunner application is not running and could not be recovered. Can't restart the server"));
        }
    }
}
