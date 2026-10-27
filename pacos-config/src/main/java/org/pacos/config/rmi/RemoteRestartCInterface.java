package org.pacos.config.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteRestartCInterface extends Remote {

    default boolean isAlive() throws RemoteException {
        return true;
    }

    void restart() throws RemoteException;
}
