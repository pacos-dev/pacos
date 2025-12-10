package org.pacos.config.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteRestartCInterface extends Remote {

    void restart() throws RemoteException;
}
