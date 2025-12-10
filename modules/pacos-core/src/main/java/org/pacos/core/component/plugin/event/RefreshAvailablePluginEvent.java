package org.pacos.core.component.plugin.event;

import org.pacos.core.component.plugin.loader.PluginInfoLoader;
import org.pacos.core.component.plugin.loader.PluginRefreshListener;

public final class RefreshAvailablePluginEvent {

    private RefreshAvailablePluginEvent() {
    }

    public static void fireEvent(PluginRefreshListener refreshListener) {
        PluginInfoLoader.refreshPluginList(refreshListener);
    }
}
