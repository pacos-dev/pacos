package org.pacos.base.api;

/**
 * Class responsible for generating a token that allows access to the pacos API.
 * The token is required for all implementations that require communication between plugins.
 */
public interface InternalApiAccess {

    /**
     * Return a token with the plugin name. The user can revoke the token from the UI, then access will be blocked.
     *
     * @param pluginName name of the plugin that wants to access the API
     * @return {@link AccessToken}
     */
    AccessToken receiveToken(String pluginName);
}
