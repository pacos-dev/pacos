package org.pacos.core.config.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * A class wrapping the original request that allows override the original url to handle it correctly on the plugin side
 * eg. /plugin/plugin_name/api will be overridden by /api
 */
public class PluginRequestWrapper extends HttpServletRequestWrapper {
    private final String newPath;

    public PluginRequestWrapper(HttpServletRequest request, String newPath) {
        super(request);
        this.newPath = newPath;
    }

    @Override
    public String getRequestURI() {
        return newPath;
    }

    @Override
    public String getServletPath() {
        return newPath;
    }
}