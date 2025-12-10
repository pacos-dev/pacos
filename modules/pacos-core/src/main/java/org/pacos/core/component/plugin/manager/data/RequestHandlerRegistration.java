package org.pacos.core.component.plugin.manager.data;

import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.shared.Registration;

/**
 * Holds reference to registration for added custom RequestResourceHandler
 */
public record RequestHandlerRegistration(Registration registration, RequestHandler resourceHandler) {

}
