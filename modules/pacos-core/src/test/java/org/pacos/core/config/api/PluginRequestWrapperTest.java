package org.pacos.core.config.api;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class PluginRequestWrapperTest {

    @Test
    public void whenWrapOriginalRequestThenReturnCorrectUrl() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/plugin/plugin_name/api");
        when(request.getServletPath()).thenReturn("/plugin/plugin_name/api");
        //when
        PluginRequestWrapper pluginRequestWrapper = new PluginRequestWrapper(request, "/api");
        //then
        assertEquals("/api", pluginRequestWrapper.getRequestURI());
        assertEquals("/api", pluginRequestWrapper.getServletPath());

    }
}