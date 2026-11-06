package org.pacos.base.credential;

import java.io.InputStream;
import java.util.Map;

public interface ResolvedCredential extends AutoCloseable {

    String getText(String fieldKey);

    char[] getSecret(String fieldKey);

    Map<String, String> getKeyValues(String fieldKey);

    InputStream openFile(String fieldKey);

    @Override
    void close();
}
