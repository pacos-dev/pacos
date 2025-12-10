package org.pacos.core.component.plugin.view.window;

import com.vaadin.flow.component.orderedlayout.Scroller;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.plugin.view.window.tab.MarketplaceView;

public class AppStoreLayout extends Scroller {

    public AppStoreLayout(PluginProxy pluginProxy) {
        setContent(new MarketplaceView(pluginProxy));
        setSizeFull();
    }
}