package org.pacos.config.rmi;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

/**
 * Restricts RMI registry and exported object endpoints to the local machine.
 */
final class LoopbackRMIServerSocketFactory implements RMIServerSocketFactory, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    static final String HOST = "127.0.0.1";

    LoopbackRMIServerSocketFactory() {
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port, 0, InetAddress.getByName(HOST));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LoopbackRMIServerSocketFactory;
    }

    @Override
    public int hashCode() {
        return LoopbackRMIServerSocketFactory.class.hashCode();
    }
}
