package org.pacos.core.component.plugin.view.window.tab;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.pacos.core.component.plugin.event.RefreshAvailablePluginEvent;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.plugin.view.plugin.PluginListView;

public class MarketplaceView extends VerticalLayout {

    public MarketplaceView(PluginProxy pluginProxy) {
        this.setSizeFull();

        PluginListView pluginListView = new PluginListView(pluginProxy);
        RefreshAvailablePluginEvent.fireEvent(pluginListView);
        add(pluginListView);
    }

}
