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
        SwaggerUiConfigProperties swaggerUiConfigProperties = new SwaggerUiConfigProperties();
        SwaggerUIConfigReload configReload = new SwaggerUIConfigReload(swaggerUiConfigProperties);
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setArtifactName("test");
        //when
        configReload.removeConfiguration(pluginDTO);
        //then
        assertFalse(swaggerUiConfigProperties.getUrls()
                .stream()
                .anyMatch(e -> e.getUrl().equals(SwaggerUIConfigReload.generateUrl(pluginDTO))));
    }

    @Test
    void whenRemovePluginWithApiThenSwaggerUIConfigReload() {
        SwaggerUiConfigProperties swaggerUiConfigProperties = new SwaggerUiConfigProperties();
        SwaggerUIConfigReload configReload = new SwaggerUIConfigReload(swaggerUiConfigProperties);
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setArtifactName("test");
        configReload.addDocumentation(pluginDTO);
        //when
        configReload.removeConfiguration(pluginDTO);
        //then
        assertFalse(swaggerUiConfigProperties.getUrls()
                .stream()
                .anyMatch(e -> e.getUrl().equals(SwaggerUIConfigReload.generateUrl(pluginDTO))));
    }

    @Test
    void whenRemovePluginThenConfigurationContainsAnotherPluginConfig() {
        SwaggerUiConfigProperties swaggerUiConfigProperties = new SwaggerUiConfigProperties();
        SwaggerUIConfigReload configReload = new SwaggerUIConfigReload(swaggerUiConfigProperties);
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setArtifactName("test");
        configReload.addDocumentation(pluginDTO);
        PluginDTO pluginDTO2 = new PluginDTO();
        pluginDTO2.setArtifactName("test2");
        //when
        configReload.removeConfiguration(pluginDTO2);
        //then
        assertTrue(swaggerUiConfigProperties.getUrls()
                .stream()
                .anyMatch(e -> e.getUrl().equals(SwaggerUIConfigReload.generateUrl(pluginDTO))));
    }

    @Test
    void whenPluginContainsApiSpecificationThenExtendSwaggerUIConfig() {
        SwaggerUiConfigProperties swaggerUiConfigProperties = new SwaggerUiConfigProperties();
        SwaggerUIConfigReload configReload = new SwaggerUIConfigReload(swaggerUiConfigProperties);
        RequestMapping requestMapping = Mockito.mock(RequestMapping.class);
        mockRequestMapping(requestMapping, "/plugin/test/v3/api-docs");

        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setArtifactName("test");
        //given
        try (MockedStatic<PluginState> pluginStateMock = Mockito.mockStatic(PluginState.class);
                MockedStatic<PluginResource> pluginResourceMock = Mockito.mockStatic(PluginResource.class)) {
            pluginStateMock.when(() -> PluginState.getState(pluginDTO)).thenReturn(PluginStatusEnum.ON);
            pluginResourceMock.when(() -> PluginResource.loadRequestMappingForPluginName("test"))
                    .thenReturn(Optional.of(requestMapping));
            //when
            configReload.addConfiguration(pluginDTO);
            //then
            assertTrue(swaggerUiConfigProperties.getUrls()
                    .stream()
                    .anyMatch(e -> e.getUrl().equals(SwaggerUIConfigReload.generateUrl(pluginDTO))));
        }
    }

    @Test
    void whenPluginDoNotContainsApiSpecificationThenDoNotExtendSwaggerUIConfig() {
        SwaggerUiConfigProperties swaggerUiConfigProperties = new SwaggerUiConfigProperties();
        SwaggerUIConfigReload configReload = new SwaggerUIConfigReload(swaggerUiConfigProperties);

        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setArtifactName("test");
        //given
        try (MockedStatic<PluginState> pluginStateMock = Mockito.mockStatic(PluginState.class);
                MockedStatic<PluginResource> pluginResourceMock = Mockito.mockStatic(PluginResource.class)) {
            pluginStateMock.when(() -> PluginState.getState(pluginDTO)).thenReturn(PluginStatusEnum.ON);
            pluginResourceMock.when(() -> PluginResource.loadRequestMappingForPluginName("test"))
                    .thenReturn(Optional.empty());
            //when
            configReload.addConfiguration(pluginDTO);
            //then
            assertFalse(swaggerUiConfigProperties.getUrls()
                    .stream()
                    .anyMatch(e -> e.getUrl().equals(SwaggerUIConfigReload.generateUrl(pluginDTO))));
        }
    }

    @Test
    void whenPluginDoNotContainsApiThenDoNotExtendSwaggerUIConfig() {
        SwaggerUiConfigProperties swaggerUiConfigProperties = new SwaggerUiConfigProperties();
        SwaggerUIConfigReload configReload = new SwaggerUIConfigReload(swaggerUiConfigProperties);
        RequestMapping requestMapping = Mockito.mock(RequestMapping.class);
        mockRequestMapping(requestMapping, "/plugin/test/v3/api-docs");

        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setArtifactName("test");
        //given
        try (MockedStatic<PluginState> pluginStateMock = Mockito.mockStatic(PluginState.class);
                MockedStatic<PluginResource> pluginResourceMock = Mockito.mockStatic(PluginResource.class)) {
            pluginStateMock.when(() -> PluginState.getState(pluginDTO)).thenReturn(PluginStatusEnum.ON);
            pluginResourceMock.when(() -> PluginResource.loadRequestMappingForPluginName("test"))
                    .thenReturn(Optional.of(requestMapping));
            //when
            configReload.addConfiguration(pluginDTO);
            //then
            assertTrue(swaggerUiConfigProperties.getUrls()
                    .stream()
                    .anyMatch(e -> e.getUrl().equals(SwaggerUIConfigReload.generateUrl(pluginDTO))));
        }
    }

    @Test
    void whenPluginNotStartThenDoNotExtendSwaggerUiConfig() {
        SwaggerUiConfigProperties swaggerUiConfigProperties = new SwaggerUiConfigProperties();
        SwaggerUIConfigReload configReload = new SwaggerUIConfigReload(swaggerUiConfigProperties);

        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setArtifactName("test");
        //given
        try (MockedStatic<PluginState> pluginStateMock = Mockito.mockStatic(PluginState.class)) {
            pluginStateMock.when(() -> PluginState.getState(pluginDTO)).thenReturn(PluginStatusEnum.ERROR);
            //when
            configReload.addConfiguration(pluginDTO);
            //then
            assertFalse(swaggerUiConfigProperties.getUrls()
                    .stream()
                    .anyMatch(e -> e.getUrl().equals(SwaggerUIConfigReload.generateUrl(pluginDTO))));
        }
    }

    private static void mockRequestMapping(RequestMapping requestMapping, String endpoint) {
        RequestMappingInfo requestMappingInfo = Mockito.mock(RequestMappingInfo.class);
        when(requestMappingInfo.getPathPatternsCondition()).thenReturn(Mockito.mock(PathPatternsRequestCondition.class));
        PathPattern pattern = Mockito.mock(PathPattern.class);
        when(pattern.getPatternString()).thenReturn(endpoint);
        when(requestMappingInfo.getPathPatternsCondition().getPatterns()).thenReturn(Set.of(pattern));
        when(requestMappingInfo.getPathPatternsCondition().getFirstPattern()).thenReturn(pattern);
        when(requestMapping.requestMappingInfoHandlerMapping()).thenReturn(Mockito.mock(RequestMappingHandlerMapping.class));

        when(requestMapping.requestMappingInfoHandlerMapping()
                .getHandlerMethods()).thenReturn(Map.of(requestMappingInfo, Mockito.mock(
                HandlerMethod.class)));
    }
}