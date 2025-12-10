package org.pacos.core.component.plugin.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;

/**
 * Default configuration to share static resources from the plugin to the pacos
 */
public class PluginRequestHandler implements RequestHandler {

    private final ClassLoader moduleClassLoader;
    private final Set<String> resources;

    public PluginRequestHandler(Set<String> resources, ClassLoader moduleClassLoader) {
        this.resources = resources;
        this.moduleClassLoader = moduleClassLoader;
    }

    @Override
    public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response) throws
            IOException {
        String path = request.getPathInfo();
        if (resources.contains(path)) {
            try (InputStream resourceStream = findResource(path)) {
                if (resourceStream != null) {
                    response.getOutputStream().write(resourceStream.readAllBytes());
                    return true;
                }
            }
        }
        return false;
    }

    private InputStream findResource(String resourcePath) {
        return moduleClassLoader.getResourceAsStream("META-INF/resources" + resourcePath);
    }

}
