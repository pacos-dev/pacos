package org.pacos.core.component.plugin.manager;

import java.util.HashSet;
import java.util.Optional;

import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.data.RequestMapping;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Reloads the swagger-ui configuration based on currently installed plugins that provide their specification
 */
@Component
public class SwaggerUIConfigReload {

    private final SwaggerUiConfigProperties swaggerUiConfigProperties;

    @Autowired
    public SwaggerUIConfigReload(SwaggerUiConfigProperties swaggerUiConfigProperties) {
        this.swaggerUiConfigProperties = swaggerUiConfigProperties;
        this.swaggerUiConfigProperties.setUrls(new HashSet<>());
    }

    void removeConfiguration(PluginDTO plugin) {
        AbstractSwaggerUiConfigProperties.SwaggerUrl toRemove = null;
        for (AbstractSwaggerUiConfigProperties.SwaggerUrl swaggerUrl : swaggerUiConfigProperties.getUrls()) {
            if (swaggerUrl.getUrl().equals(generateUrl(plugin))) {
                toRemove = swaggerUrl;
                break;
            }
        }
        if (toRemove != null) {
            swaggerUiConfigProperties.getUrls().remove(toRemove);
        }
    }

    void addConfiguration(PluginDTO plugin) {
        if (PluginState.getState(plugin).isOn()) {
            Optional<RequestMapping> requestMapping =
                    PluginResource.loadRequestMappingForPluginName(plugin.getArtifactName());
            if (requestMapping.isPresent()) {
                boolean apiDocConfigured =
                        requestMapping.get().requestMappingInfoHandlerMapping().getHandlerMethods().keySet().stream()
                                .filter(e -> !e.getPathPatternsCondition().getPatterns().isEmpty())
                                .anyMatch(e -> e.getPathPatternsCondition()
                                        .getFirstPattern()
                                        .getPatternString()
                                        .equals("/" + generateUrl(plugin)));
                if (apiDocConfigured) {
                    addDocumentation(plugin);
                }
            }
        }
    }

    void addDocumentation(PluginDTO plugin) {
        AbstractSwaggerUiConfigProperties.SwaggerUrl swaggerUrl =
                new AbstractSwaggerUiConfigProperties.SwaggerUrl(
                        generateUrl(plugin),
                        generateUrl(plugin),
                        "Plugin " + plugin.getArtifactName());

        swaggerUiConfigProperties.getUrls().add(swaggerUrl);
    }

    static String generateUrl(PluginDTO plugin) {
        return "plugin/" + plugin.getArtifactName() + "/v3/api-docs";
    }
}
