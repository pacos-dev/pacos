package org.pacos.core.config.api;

import java.io.IOException;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.pacos.core.component.plugin.manager.PluginResource;
import org.pacos.core.component.plugin.manager.data.RequestMapping;
import org.pacos.core.component.token.service.ApiTokenService;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PluginRequestFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private RequestMappingHandlerMapping handlerMapping;

    @Mock
    private RequestMappingHandlerAdapter handlerAdapter;

    @Mock
    private HandlerExecutionChain handlerExecutionChain;

    @Mock
    private ApiTokenService tokenService;

    @InjectMocks
    private PluginRequestFilter pluginRequestFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(request.getHeader("Authorization")).thenReturn("api- token");
        when(tokenService.isValidToken("api- token")).thenReturn(true);
    }

    @Test
    void whenInvalidUrlThenReturnNotFound() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/plugin/");
        pluginRequestFilter.doFilter(request, response, filterChain);
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void whenPluginNotFoundThenCallNextFilter() throws IOException {
        when(request.getRequestURI()).thenReturn("/plugin/nonexistent");
        try (MockedStatic<PluginResource> pluginResourceMock = mockStatic(PluginResource.class)) {
            pluginResourceMock.when(() -> PluginResource.loadRequestMappingForPluginName("nonexistent"))
                    .thenReturn(Optional.empty());
            //when
            pluginRequestFilter.doFilter(request, response, filterChain);
            //then
            verify(filterChain).doFilter(request, response);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void whenNpApiConfigurationThenReturnNotFound() throws Exception {
        RequestMapping requestMapping = mock(RequestMapping.class);
        when(request.getRequestURI()).thenReturn("/plugin/test");
        try (MockedStatic<PluginResource> pluginResourceMock = mockStatic(PluginResource.class)) {

            pluginResourceMock.when(() -> PluginResource.loadRequestMappingForPluginName("test"))
                    .thenReturn(Optional.of(requestMapping));
            when(requestMapping.requestMappingInfoHandlerMapping()).thenReturn(handlerMapping);
            when(handlerMapping.getHandler(any())).thenReturn(null);

            pluginRequestFilter.doFilter(request, response, filterChain);

            verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Plugin does not have configured API: 'test'");
        }
    }

    @Test
    void whenHandlerNotFoundThenCallNextFilter() throws Exception {
        RequestMapping requestMapping = mock(RequestMapping.class);
        when(requestMapping.requestMappingInfoHandlerMapping()).thenReturn(new RequestMappingHandlerMapping());
        when(requestMapping.requestMappingHandlerAdapter()).thenReturn(new RequestMappingHandlerAdapter());
        when(request.getRequestURI()).thenReturn("/plugin/test");
        try (MockedStatic<PluginResource> pluginResourceMock = mockStatic(PluginResource.class)) {

            pluginResourceMock.when(() -> PluginResource.loadRequestMappingForPluginName("test"))
                    .thenReturn(Optional.of(requestMapping));
            when(requestMapping.requestMappingInfoHandlerMapping()).thenReturn(handlerMapping);
            when(handlerMapping.getHandler(any())).thenReturn(null);

            pluginRequestFilter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    void whenHandlerExistsThenInvokeHandler() throws Exception {
        RequestMapping requestMapping = mock(RequestMapping.class);
        Object handler = new Object();

        when(request.getRequestURI()).thenReturn("/plugin/test/some-api");
        try (MockedStatic<PluginResource> pluginResourceMock = mockStatic(PluginResource.class)) {

            pluginResourceMock.when(() -> PluginResource.loadRequestMappingForPluginName("test"))
                    .thenReturn(Optional.of(requestMapping));
            when(requestMapping.requestMappingInfoHandlerMapping()).thenReturn(handlerMapping);
            when(requestMapping.requestMappingHandlerAdapter()).thenReturn(handlerAdapter);
            when(handlerMapping.getHandler(any())).thenReturn(handlerExecutionChain);
            when(handlerExecutionChain.getHandler()).thenReturn(handler);

            pluginRequestFilter.doFilter(request, response, filterChain);

            verify(handlerAdapter).handle(any(), any(), eq(handler));
        }
    }

    @Test
    void whenErrorWhileInvokeHandlerThenReturn500() throws Exception {
        RequestMapping requestMapping = mock(RequestMapping.class);
        Object handler = new Object();

        when(request.getRequestURI()).thenReturn("/plugin/test/some-api");
        try (MockedStatic<PluginResource> pluginResourceMock = mockStatic(PluginResource.class)) {

            pluginResourceMock.when(() -> PluginResource.loadRequestMappingForPluginName("test"))
                    .thenReturn(Optional.of(requestMapping));
            when(requestMapping.requestMappingInfoHandlerMapping()).thenReturn(handlerMapping);
            when(requestMapping.requestMappingHandlerAdapter()).thenReturn(handlerAdapter);
            when(handlerMapping.getHandler(any())).thenReturn(handlerExecutionChain);
            when(handlerExecutionChain.getHandler()).thenReturn(handler);
            doThrow(new Exception("Error msg")).when(handlerAdapter).handle(any(), any(), eq(handler));

            pluginRequestFilter.doFilter(request, response, filterChain);

            verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error msg");
        }
    }

    @Test
    void whenAuthorizationHeaderMissingThenReturnError() throws Exception {
        RequestMapping requestMapping = mock(RequestMapping.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/plugin/test/some-api");
        try (MockedStatic<PluginResource> pluginResourceMock = mockStatic(PluginResource.class)) {

            pluginResourceMock.when(() -> PluginResource.loadRequestMappingForPluginName("test"))
                    .thenReturn(Optional.of(requestMapping));
            when(requestMapping.requestMappingInfoHandlerMapping()).thenReturn(handlerMapping);
            when(requestMapping.requestMappingHandlerAdapter()).thenReturn(handlerAdapter);

            pluginRequestFilter.doFilter(request, response, filterChain);

            verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Test
    void whenTokenInvalidThenReturnError() throws Exception {
        RequestMapping requestMapping = mock(RequestMapping.class);
        when(request.getHeader("Authorization")).thenReturn("token");
        when(tokenService.isValidToken("token")).thenReturn(false);
        when(request.getRequestURI()).thenReturn("/plugin/test/some-api");
        try (MockedStatic<PluginResource> pluginResourceMock = mockStatic(PluginResource.class)) {

            pluginResourceMock.when(() -> PluginResource.loadRequestMappingForPluginName("test"))
                    .thenReturn(Optional.of(requestMapping));
            when(requestMapping.requestMappingInfoHandlerMapping()).thenReturn(handlerMapping);
            when(requestMapping.requestMappingHandlerAdapter()).thenReturn(handlerAdapter);

            pluginRequestFilter.doFilter(request, response, filterChain);

            verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}