package org.pacos.core.component.credential.service;

import java.io.Serializable;

/**
 * Internal authorization identity created by PacOS core.
 *
 * <p>User contexts are evaluated against group grants. Plugin and application contexts are evaluated
 * against their respective grants and never inherit user group grants or shared visibility. Plugin-facing APIs must bind plugin identity
 * in the host and must not accept this context from plugin code.</p>
 */
public record CredentialAccessContext(Integer userId, String pluginId, String applicationId) implements Serializable {

    public static final String PACOS_APPLICATION_ID = "pacos";

    public CredentialAccessContext(Integer userId, String pluginId) {
        this(userId, pluginId, null);
    }

    public CredentialAccessContext {
        if (userId == null && (pluginId == null || pluginId.isBlank())
                && (applicationId == null || applicationId.isBlank())) {
            throw new IllegalArgumentException("A user, plugin or application identity is required");
        }
        int identities = (userId == null ? 0 : 1) + (pluginId == null ? 0 : 1) + (applicationId == null ? 0 : 1);
        if (identities > 1 && !(userId != null && pluginId != null && applicationId == null)) {
            throw new IllegalArgumentException("Application identity cannot be combined with another principal");
        }
        if (userId != null && userId < 1) {
            throw new IllegalArgumentException("User id must be positive");
        }
        if (applicationId != null && !PACOS_APPLICATION_ID.equals(applicationId)) {
            throw new IllegalArgumentException("Unsupported application identity");
        }
        if (pluginId != null && (pluginId.isBlank() || pluginId.length() > 255)) {
            throw new IllegalArgumentException("Plugin id must contain between 1 and 255 characters");
        }
    }

    public static CredentialAccessContext forApplication() {
        return new CredentialAccessContext(null, null, PACOS_APPLICATION_ID);
    }

    public static CredentialAccessContext forApplication(String applicationId) {
        if (!PACOS_APPLICATION_ID.equals(applicationId)) {
            throw new IllegalArgumentException("Unsupported application identity");
        }
        return forApplication();
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
