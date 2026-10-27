package org.pacos.config.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteRestartCInterface extends Remote {

    boolean isAlive() throws RemoteException;

    void restart() throws RemoteException;
}
