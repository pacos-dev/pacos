package org.pacos.config.rmi;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LoopbackRMIServerSocketFactoryTest {

    @Test
    void whenServerSocketIsCreatedThenItBindsOnlyToLoopback() throws IOException {
        try (ServerSocket socket = LoopbackRMIServerSocketFactory.INSTANCE.createServerSocket(0)) {
            assertTrue(socket.getInetAddress().isLoopbackAddress());
        }
    }
}
