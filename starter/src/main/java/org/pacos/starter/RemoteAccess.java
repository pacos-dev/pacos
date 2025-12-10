package org.pacos.starter;

import java.rmi.RemoteException;

import org.pacos.config.rmi.RemoteRestartCInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteAccess implements RemoteRestartCInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteAccess.class);
    private final Pacos pacos;

    public RemoteAccess(Pacos pacos) {
        this.pacos = pacos;
    }

    @Override
    public void restart() throws RemoteException {
        LOGGER.info("Restart event triggered");
        pacos.restartEngine();
    }
}
