package org.pacos.core.component.plugin.manager.data;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestMappingTest {

    @Test
    void whenCreateRequestMappingThenFieldsAreSetCorrectly() {
        RequestMappingHandlerMapping handlerMapping = Mockito.mock(RequestMappingHandlerMapping.class);
        RequestMappingHandlerAdapter handlerAdapter = Mockito.mock(RequestMappingHandlerAdapter.class);

        RequestMapping requestMapping = new RequestMapping(handlerMapping, handlerAdapter);

        assertEquals(handlerMapping, requestMapping.requestMappingInfoHandlerMapping());
        assertEquals(handlerAdapter, requestMapping.requestMappingHandlerAdapter());
    }
}