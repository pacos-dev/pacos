package org.pacos.config.rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RemoteRegistryServiceTest {

    @Test
    void whenInterfaceIsRegisteredInTheRmiRegistryThenIsAccessibleByAnotherApplication() throws RemoteException, NotBoundException {
        System.setProperty("-Drmi.port", "1999");
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