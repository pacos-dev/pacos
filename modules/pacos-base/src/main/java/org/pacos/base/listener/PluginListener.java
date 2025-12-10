package org.pacos.base.listener;

import org.springframework.context.ApplicationContext;

/**
 * This listener is called every time any plugin is installed.
 */
public interface PluginListener {

    /**
     * Called when plugin context was initialized
     */
    void pluginInitialized(ApplicationContext context);

    /**
     * Called when plugin is removed
     */
    void pluginRemoved(ApplicationContext context);
}
