package org.pacos.core.component.plugin.event;

import org.pacos.config.repository.data.AppRepository;
import org.pacos.core.component.plugin.loader.PluginInfoLoader;
import org.pacos.core.component.plugin.loader.PluginRefreshListener;

public final class RefreshAvailablePluginEvent {

    private RefreshAvailablePluginEvent() {
    }

    public static void fireEvent(PluginRefreshListener refreshListener) {
        PluginInfoLoader.refreshPluginList(refreshListener);
    }

    public static void fireEvent(AppRepository repository, PluginRefreshListener refreshListener) {
        PluginInfoLoader.refreshPluginList(repository, refreshListener);
    }

    public static void fireEvent(AppRepository repository, Object requestOwner,
                                 PluginRefreshListener refreshListener) {
        PluginInfoLoader.refreshPluginList(repository, requestOwner, refreshListener);
    }
}
