package org.pacos.config.rmi;

import java.net.ServerSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Isolated("Shares JVM-wide system properties or other global test resources; keep isolated during parallel runs.")
class RemoteRegistryServiceTest {
    private String originalRmiPort;

    @BeforeEach
    void setRmiPort() {
        originalRmiPort = System.getProperty("rmi.port");
        System.setProperty("rmi.port", "1999");
    }

    @AfterEach
    void restoreRmiPort() {
        if (originalRmiPort == null) {
            System.clearProperty("rmi.port");
        } else {
            System.setProperty("rmi.port", originalRmiPort);
        }
    }


    @Test
    void whenInterfaceIsRegisteredInTheRmiRegistryThenIsAccessibleByAnotherApplication() throws RemoteException, NotBoundException {
        RemoteRestartCInterface in = () -> {
        };
        //when
        RemoteRegistryService.registerRemoteInterface(in);
        //then
        RemoteRestartCInterface remote = RemoteRegistryService.loadRemoteInterface();

        assertNotNull(remote);
    }

    @Test
    void whenRegisterRemoteDoubleTimeThenNoException() {
        RemoteRestartCInterface in = () -> {
        };
        //when
        RemoteRegistryService.registerRemoteInterface(in);
        //then
        assertDoesNotThrow(() ->
                RemoteRegistryService.registerRemoteInterface(in));
    }

    @Test
    void whenConfiguredRmiPortIsMalformedThenRegistrationDoesNotBlockStartup() {
        System.setProperty("rmi.port", "not-a-port");

        assertDoesNotThrow(() ->
                RemoteRegistryService.registerRemoteInterface((RemoteRestartCInterface) () -> {
                }));
    }

    @Test
    void whenPortIsOwnedByAnotherRmiRegistryThenRegistrationDoesNotRebindIntoIt() throws Exception {
        int occupiedPort;
        try (ServerSocket reservation = new ServerSocket(0)) {
            occupiedPort = reservation.getLocalPort();
        }

        Registry foreignRegistry = LocateRegistry.createRegistry(occupiedPort);
        try {
            System.setProperty("rmi.port", Integer.toString(occupiedPort));

            assertDoesNotThrow(() ->
                    RemoteRegistryService.registerRemoteInterface((RemoteRestartCInterface) () -> {
                    }));
            assertFalse(Arrays.asList(foreignRegistry.list()).contains("remoteRestartInterface"));
        } finally {
            UnicastRemoteObject.unexportObject(foreignRegistry, true);
        }
    }
}
