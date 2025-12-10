package org.pacos.core.component.plugin.manager.data;

import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * Holds the objects necessary to redirect a http request.
 */
public record RequestMapping(RequestMappingInfoHandlerMapping requestMappingInfoHandlerMapping,
                             RequestMappingHandlerAdapter requestMappingHandlerAdapter) {

}
