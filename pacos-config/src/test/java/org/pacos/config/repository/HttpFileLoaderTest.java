package org.pacos.config.repository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpFileLoaderTest {

    @Test
    void whenGetFileContentThenSetTimeoutsAndDisconnectConnection() throws IOException {
        URL baseUrl = new URL("http://localhost/repository");
        StubHttpURLConnection connection = new StubHttpURLConnection(baseUrl);
        URL url = new URL(null, baseUrl.toExternalForm(), new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL url) {
                return connection;
            }
        });

        String content = HttpFileLoader.getFileContent(url);

        assertEquals("pacosruntime", content);
        assertEquals("GET", connection.getRequestMethod());
        assertEquals(HttpFileLoader.DEFAULT_CONNECT_TIMEOUT_MILLIS, connection.getConnectTimeout());
        assertEquals(HttpFileLoader.DEFAULT_READ_TIMEOUT_MILLIS, connection.getReadTimeout());
        assertTrue(connection.disconnected);
    }

    @Test
    void whenGetFileContentFailsThenDisconnectConnection() throws IOException {
        URL baseUrl = new URL("http://localhost/repository");
        StubHttpURLConnection connection = new StubHttpURLConnection(baseUrl);
        connection.failOnRead = true;
        URL url = new URL(null, baseUrl.toExternalForm(), new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL url) {
                return connection;
            }
        });

        assertThrows(IOException.class, () -> HttpFileLoader.getFileContent(url));
        assertTrue(connection.disconnected);
    }

    private static final class StubHttpURLConnection extends HttpURLConnection {
        private boolean disconnected;
        private boolean failOnRead;

        private StubHttpURLConnection(URL url) {
            super(url);
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
