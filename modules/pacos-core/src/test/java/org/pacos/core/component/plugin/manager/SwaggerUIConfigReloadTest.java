package org.pacos.core.component.plugin.manager;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.data.RequestMapping;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class SwaggerUIConfigReloadTest {

    @Test
    void whenRemovePluginWithoutApiThenSwaggerUIConfigReload() {
        SwaggerUiConfigProperties properties = new SwaggerUiConfigProperties();
        SwaggerUIConfigReload configReload = new SwaggerUIConfigReload(properties, new PluginState());
        PluginDTO plugin = plugin("test");
        configReload.removeConfiguration(plugin);
        assertFalse(properties.getUrls().stream()
                .anyMatch(url -> url.getUrl().equals(SwaggerUIConfigReload.generateUrl(plugin))));
    }

    @Test
    void whenRemovePluginWithApiThenSwaggerUIConfigReload() {
        SwaggerUiConfigProperties properties = new SwaggerUiConfigProperties();
        SwaggerUIConfigReload configReload = new SwaggerUIConfigReload(properties, new PluginState());
        PluginDTO plugin = plugin("test");
        configReload.addDocumentation(plugin);
        configReload.removeConfiguration(plugin);
        assertFalse(properties.getUrls().stream()
                .anyMatch(url -> url.getUrl().equals(SwaggerUIConfigReload.generateUrl(plugin))));
    }

    @Test
    void whenRemovePluginThenConfigurationContainsAnotherPluginConfig() {
        SwaggerUiConfigProperties properties = new SwaggerUiConfigProperties();
        SwaggerUIConfigReload configReload = new SwaggerUIConfigReload(properties, new PluginState());
        PluginDTO plugin = plugin("test");
        configReload.addDocumentation(plugin);
        configReload.removeConfiguration(plugin("test2"));
        assertTrue(properties.getUrls().stream()
                .anyMatch(url -> url.getUrl().equals(SwaggerUIConfigReload.generateUrl(plugin))));
    }

    @Test
    void whenPluginContainsApiSpecificationThenExtendSwaggerUIConfig() {
        SwaggerUiConfigProperties properties = new SwaggerUiConfigProperties();
        PluginState state = new PluginState();
        PluginDTO plugin = plugin("test");
        state.setState(plugin, PluginStatusEnum.ON);
        SwaggerUIConfigReload configReload = new SwaggerUIConfigReload(properties, state);
        RequestMapping requestMapping = Mockito.mock(RequestMapping.class);
        mockRequestMapping(requestMapping, "/plugin/test/v3/api-docs");

        try (MockedStatic<PluginResource> resourceMock = Mockito.mockStatic(PluginResource.class)) {
            resourceMock.when(() -> PluginResource.loadRequestMappingForPluginName("test"))
                    .thenReturn(Optional.of(requestMapping));
            configReload.addConfiguration(plugin);
        }
        assertTrue(properties.getUrls().stream()
                .anyMatch(url -> url.getUrl().equals(SwaggerUIConfigReload.generateUrl(plugin))));
    }

    @Test
    void whenPluginDoesNotContainApiSpecificationThenDoNotExtendSwaggerUIConfig() {
        SwaggerUiConfigProperties properties = new SwaggerUiConfigProperties();
        PluginState state = new PluginState();
        PluginDTO plugin = plugin("test");
        state.setState(plugin, PluginStatusEnum.ON);
        SwaggerUIConfigReload configReload = new SwaggerUIConfigReload(properties, state);

        try (MockedStatic<PluginResource> resourceMock = Mockito.mockStatic(PluginResource.class)) {
            resourceMock.when(() -> PluginResource.loadRequestMappingForPluginName("test"))
                    .thenReturn(Optional.empty());
            configReload.addConfiguration(plugin);
        }
        assertFalse(properties.getUrls().stream()
                .anyMatch(url -> url.getUrl().equals(SwaggerUIConfigReload.generateUrl(plugin))));
    }

    @Test
    void whenPluginDoesNotStartThenDoNotExtendSwaggerUiConfig() {
        SwaggerUiConfigProperties properties = new SwaggerUiConfigProperties();
        PluginState state = new PluginState();
        PluginDTO plugin = plugin("test");
        state.setState(plugin, PluginStatusEnum.ERROR);
        SwaggerUIConfigReload configReload = new SwaggerUIConfigReload(properties, state);
        configReload.addConfiguration(plugin);
        assertFalse(properties.getUrls().stream()
                .anyMatch(url -> url.getUrl().equals(SwaggerUIConfigReload.generateUrl(plugin))));
    }

    private static PluginDTO plugin(String artifactName) {
        PluginDTO plugin = new PluginDTO();
        plugin.setArtifactName(artifactName);
        return plugin;
    }

    private static void mockRequestMapping(RequestMapping requestMapping, String endpoint) {
        RequestMappingInfo info = Mockito.mock(RequestMappingInfo.class);
        PathPatternsRequestCondition condition = Mockito.mock(PathPatternsRequestCondition.class);
        when(info.getPathPatternsCondition()).thenReturn(condition);
        PathPattern pattern = Mockito.mock(PathPattern.class);
        when(pattern.getPatternString()).thenReturn(endpoint);
        when(condition.getPatterns()).thenReturn(Set.of(pattern));
        when(condition.getFirstPattern()).thenReturn(pattern);
        RequestMappingHandlerMapping mapping = Mockito.mock(RequestMappingHandlerMapping.class);
        when(requestMapping.requestMappingInfoHandlerMapping()).thenReturn(mapping);
        when(mapping.getHandlerMethods()).thenReturn(Map.of(info, Mockito.mock(HandlerMethod.class)));
    }
}
