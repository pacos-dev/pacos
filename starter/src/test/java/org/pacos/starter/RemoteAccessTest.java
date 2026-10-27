package org.pacos.starter;

import java.rmi.RemoteException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RemoteAccessTest {

    @Test
    void whenRestartThenEngineRestarted() throws RemoteException {
        Pacos pacos = mock(Pacos.class);
        RemoteAccess remoteAccess = new RemoteAccess(pacos);

        remoteAccess.restart();

        verify(pacos, times(1)).restartEngine();
    }

    @Test
    void whenHealthCheckedThenReturnTrue() throws RemoteException {
        RemoteAccess remoteAccess = new RemoteAccess(mock(Pacos.class));

        assertTrue(remoteAccess.isAlive());
    }
}
