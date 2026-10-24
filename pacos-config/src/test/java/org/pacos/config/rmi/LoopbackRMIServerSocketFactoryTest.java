package org.pacos.config.rmi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.jupiter.api.Test;

class LoopbackRMIServerSocketFactoryTest {

    @Test
    void whenServerSocketIsCreatedThenItBindsOnlyToLoopback() throws IOException {
        try (ServerSocket socket = new LoopbackRMIServerSocketFactory().createServerSocket(0)) {
            assertTrue(socket.getInetAddress().isLoopbackAddress());
        }
    }

    @Test
    void whenSeparateFactoriesAreComparedThenTheyAreEqual() {
        LoopbackRMIServerSocketFactory first = new LoopbackRMIServerSocketFactory();
        LoopbackRMIServerSocketFactory second = new LoopbackRMIServerSocketFactory();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
