package org.pacos.config.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class HttpFileLoaderTest {

    @Test
    void whenGetFileContentThenSetTimeoutsAndDisconnectConnection() throws IOException {
        StubHttpURLConnection connection = new StubHttpURLConnection();

        String content = HttpFileLoader.getFileContent(connection);

        assertEquals("pacosruntime", content);
        assertEquals("GET", connection.getRequestMethod());
        assertEquals(HttpFileLoader.DEFAULT_CONNECT_TIMEOUT_MILLIS, connection.getConnectTimeout());
        assertEquals(HttpFileLoader.DEFAULT_READ_TIMEOUT_MILLIS, connection.getReadTimeout());
        assertTrue(connection.disconnected);
    }

    @Test
    void whenGetFileContentFailsThenDisconnectConnection() throws IOException {
        StubHttpURLConnection connection = new StubHttpURLConnection();
        connection.failOnRead = true;

        assertThrows(IOException.class, () -> HttpFileLoader.getFileContent(connection));
        assertTrue(connection.disconnected);
    }

    private static final class StubHttpURLConnection extends HttpURLConnection {
        private boolean disconnected;
        private boolean failOnRead;

        private StubHttpURLConnection() throws IOException {
            super(URI.create("http://localhost/repository").toURL());
        }

        @Override
        public void disconnect() {
            disconnected = true;
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public void connect() {
            //not implemented
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (failOnRead) {
                throw new IOException("Connection failed");
            }
            return new ByteArrayInputStream("pacos\nruntime\n".getBytes(StandardCharsets.UTF_8));
        }
    }
}
