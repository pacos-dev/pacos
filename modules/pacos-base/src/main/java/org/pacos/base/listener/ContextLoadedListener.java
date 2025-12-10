package org.pacos.base.listener;

import com.vaadin.flow.server.VaadinService;

/**
 *
 */
@FunctionalInterface
public interface ContextLoadedListener {
    void onSystemInitialized(VaadinService vaadinService);
}
