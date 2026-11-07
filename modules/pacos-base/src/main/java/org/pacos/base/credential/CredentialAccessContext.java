package org.pacos.base.credential;

import java.io.Serializable;

public record CredentialAccessContext(Integer userId, String pluginId) implements Serializable {

    public CredentialAccessContext {
        if (userId == null && (pluginId == null || pluginId.isBlank())) {
            throw new IllegalArgumentException("A user or plugin identity is required");
        }
        if (userId != null && userId < 1) {
            throw new IllegalArgumentException("User id must be positive");
        }
        if (pluginId != null && (pluginId.isBlank() || pluginId.length() > 255)) {
            throw new IllegalArgumentException("Plugin id must contain between 1 and 255 characters");
        }
    }

    public static CredentialAccessContext forUser(int userId) {
        return new CredentialAccessContext(userId, null);
    }

    public static CredentialAccessContext forPlugin(String pluginId) {
        return new CredentialAccessContext(null, pluginId);
    }

    public static CredentialAccessContext forUserAndPlugin(int userId, String pluginId) {
        return new CredentialAccessContext(userId, pluginId);
    }
}
