package org.pacos.config.rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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


}