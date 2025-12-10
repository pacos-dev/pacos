package org.pacos.core.config.api;

import org.pacos.core.component.token.service.ApiTokenService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of a http filter that will handle all requests starting with /plugin and pass them to the
 * appropriate plugin
 */
@Configuration
public class PluginFilterConfiguration {

    @Bean
    public FilterRegistrationBean<PluginRequestFilter> pluginRequestFilterRegistration(ApiTokenService tokenService) {
        FilterRegistrationBean<PluginRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new PluginRequestFilter(tokenService));
        registrationBean.addUrlPatterns("/plugin/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}