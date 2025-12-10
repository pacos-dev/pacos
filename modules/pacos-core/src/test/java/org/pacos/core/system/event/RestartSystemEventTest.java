package org.pacos.core.system.event;

import java.rmi.RemoteException;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.config.rmi.RemoteRegistryService;
import org.pacos.config.rmi.RemoteRestartCInterface;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

class RestartSystemEventTest {

    @Test
    void whenFireEventThenReturnTrue() throws RemoteException {
        try (MockedStatic<RemoteRegistryService> mockedRemoteRegistryService = mockStatic(RemoteRegistryService.class)) {
            // Arrange
            RemoteRestartCInterface remoteInterface = mock(RemoteRestartCInterface.class);
            mockedRemoteRegistryService.when(RemoteRegistryService::loadRemoteInterface).thenReturn(remoteInterface);

            // when
            boolean result = RestartSystemEvent.fireEvent();

            // Assert
            assertTrue(result);
            verify(remoteInterface).restart();
        }
    }

    @Test
    void whenFireEventAndExceptionThenReturnFalse() {
        try (MockedStatic<RemoteRegistryService> mockedRemoteRegistryService = mockStatic(RemoteRegistryService.class)) {
            // Arrange
            mockedRemoteRegistryService.when(RemoteRegistryService::loadRemoteInterface)
                    .thenThrow(new RuntimeException("Mocked Exception"));

            try (MockedStatic<NotificationUtils> mockedNotificationUtils = mockStatic(NotificationUtils.class)) {
                // when
                boolean result = RestartSystemEvent.fireEvent();

                // Assert
                assertFalse(result);
                mockedNotificationUtils.verify(() ->
                        NotificationUtils.error("Looks like the PacOSRunner application is not running. Can't restart the server")
                );
            }
        }
    }
}
