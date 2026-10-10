package org.pacos.config.rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RemoteRegistryServiceTest {

    private String originalPort;

    @BeforeEach
    void savePort() {
        originalPort = System.getProperty("rmi.port");
    }

    @AfterEach
    void restorePort() {
        if (originalPort == null) {
            System.clearProperty("rmi.port");
        } else {
            System.setProperty("rmi.port", originalPort);
        }
    }

    @Test
    void whenInterfaceIsRegisteredInTheRmiRegistryThenIsAccessibleByAnotherApplication() throws RemoteException, NotBoundException {
        System.setProperty("rmi.port", "1999");
        RemoteRestartCInterface remoteInterface = () -> {
        };

        RemoteRegistryService.registerRemoteInterface(remoteInterface);
        RemoteRestartCInterface remote = RemoteRegistryService.loadRemoteInterface();

        assertNotNull(remote);
    }

    @Test
    void whenRegisterRemoteInterfaceWithAlreadyExportedRemoteThenThrowIllegalStateException() {
        RemoteRestartCInterface remoteInterface = () -> {
        };
        RemoteRegistryService.registerRemoteInterface(remoteInterface);

        assertThrows(IllegalStateException.class, () -> RemoteRegistryService.registerRemoteInterface(remoteInterface));
    }
}
