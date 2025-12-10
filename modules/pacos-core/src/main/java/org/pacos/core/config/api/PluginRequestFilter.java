package org.pacos.core.config.api;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.pacos.core.component.plugin.manager.PluginResource;
import org.pacos.core.component.token.service.ApiTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExecutionChain;

/**
 * The filter is responsible for redirecting traffic to the appropriate plugin
 */
@WebFilter(urlPatterns = "/plugin/*")
public class PluginRequestFilter implements Filter {

    private final Logger LOG = LoggerFactory.getLogger(PluginRequestFilter.class);
    private final ApiTokenService tokenService;

    public PluginRequestFilter(ApiTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String originalPath = request.getRequestURI();
        String[] parts = originalPath.split("/");
        if (parts.length < 3) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String pluginName = parts[2];
        var requestMappingOpt = PluginResource.loadRequestMappingForPluginName(pluginName);
        //redirect to default if plugin not found
        if (requestMappingOpt.isEmpty()) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (requestMappingOpt.get().requestMappingInfoHandlerMapping() == null
                || requestMappingOpt.get().requestMappingHandlerAdapter() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Plugin does not have configured API: '" + pluginName + "'");
            return;
        }
        if (!originalPath.endsWith("/v3/api-docs")) {
            String authToken = ((HttpServletRequest) servletRequest).getHeader("Authorization");
            if (authToken == null || !tokenService.isValidToken(authToken)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        PluginRequestWrapper wrappedRequest = new PluginRequestWrapper(request, originalPath);
        try {
            HandlerExecutionChain handlerChain =
                    requestMappingOpt.get().requestMappingInfoHandlerMapping().getHandler(wrappedRequest);
            if (handlerChain != null) {
                LOG.trace("Forwarding request to plugin API {}", originalPath);
                Object handler = handlerChain.getHandler();
                requestMappingOpt.get().requestMappingHandlerAdapter().handle(wrappedRequest, response, handler);
            } else {
                filterChain.doFilter(servletRequest, servletResponse);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}