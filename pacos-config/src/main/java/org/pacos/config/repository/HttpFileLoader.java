package org.pacos.config.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Loads text file from given URL
 */
public final class HttpFileLoader {

    static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 5_000;
    static final int DEFAULT_READ_TIMEOUT_MILLIS = 10_000;

    private HttpFileLoader() {
    }

    static String getFileContent(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLIS);
            connection.setReadTimeout(DEFAULT_READ_TIMEOUT_MILLIS);

            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }
}
