package org.pacos.core.component.plugin.manager;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.server.RequestHandler;
import org.pacos.base.event.ModuleEvent;
import org.pacos.core.component.plugin.manager.data.PluginDataLoader;
import org.pacos.core.component.plugin.manager.data.RequestHandlerRegistration;
import org.pacos.core.component.session.service.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers plugin-provided extensions with PacOS and removes them during shutdown.
 */
public class PluginExtensionRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(PluginExtensionRegistry.class);

    public void notifyWindowsRemoved(PluginDataLoader pluginData) {
        pluginData.getWindowConfigSet().forEach(windowConfig ->
                ServiceListener.notifyAll(ModuleEvent.MODULE_REMOVED, windowConfig));
    }

    public void register(PluginDataLoader pluginData) {
        for (RequestHandler handler : pluginData.getRequestHandlers()) {
            pluginData.addRequestHandlerRegistration(
                    new RequestHandlerRegistration(ServiceListener.addRequestHandler(handler), handler));
        }
        ServiceListener.addVariableProviders(pluginData.getVariableProviders());
    }

    public void unregister(PluginDataLoader pluginData) {
        List<RuntimeException> failures = new ArrayList<>();
        for (RequestHandlerRegistration handler : pluginData.getRequestHandlerRegistration()) {
            try {
                ServiceListener.removeRequestHandler(handler.resourceHandler());
            } catch (RuntimeException e) {
                failures.add(e);
            }
            try {
                handler.registration().remove();
            } catch (RuntimeException e) {
                failures.add(e);
            }
        }
        try {
            ServiceListener.removeVariableProviders(pluginData.getVariableProviders());
        } catch (RuntimeException e) {
            failures.add(e);
        }
        if (!failures.isEmpty()) {
            RuntimeException primary = failures.get(0);
            failures.stream().skip(1).forEach(primary::addSuppressed);
            LOG.warn("Failed to unregister one or more plugin extensions", primary);
            throw primary;
        }
    }
}
