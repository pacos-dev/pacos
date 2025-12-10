package org.pacos.core.config.api;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.core.component.token.service.ApiTokenService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PluginFilterConfigurationTest {

    @Test
    void whenConfigureFilterThenAddCorrectUrlPattern() {
        FilterRegistrationBean<PluginRequestFilter> registrationBean =
                new PluginFilterConfiguration().pluginRequestFilterRegistration(
                        Mockito.mock(ApiTokenService.class));
        //then
        assertEquals(1, registrationBean.getUrlPatterns().size());
        assertEquals("/plugin/*", new ArrayList<>(registrationBean.getUrlPatterns()).get(0));
    }
}