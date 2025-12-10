package org.pacos.core.component.plugin.view.plugin;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import org.pacos.base.component.NoContent;
import org.pacos.base.component.Spinner;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.loader.PluginRefreshListener;
import org.pacos.core.component.plugin.proxy.PluginProxy;

public class PluginListView extends Div implements PluginRefreshListener {

    private final transient PluginProxy pluginProxy;
    private final UI ui;

    public PluginListView(PluginProxy pluginProxy) {
        this.setSizeFull();
        this.pluginProxy = pluginProxy;
        add(new Spinner().center());
        this.ui = UI.getCurrent();
    }

    @Override
    public void listLoaded(List<Plugin> pluginList) {
        List<PluginDTO> installedPlugins = pluginProxy.getPluginService().findAll();
        List<PluginDTO> pluginsFromRepo = new ArrayList<>();
        if (pluginList != null) {
            pluginsFromRepo = pluginList.stream().map(PluginDTO::new).toList();
        }

        PluginHandler handler = new PluginHandler(installedPlugins, pluginsFromRepo, AppRepository.pluginRepo());

        ui.access(() -> {
            reloadViewInBackground(pluginList, handler);
            ui.push();
        });
    }

    private void reloadViewInBackground(List<Plugin> pluginList, PluginHandler handler) {
        removeAll();
        if (pluginList == null) {
            NotificationUtils.error("Can't load plugin list from remote server. " +
                    "Check your internet connection or firewall configuration");
        }

        if (handler.getPluginToDisplay().isEmpty()) {
            add(new NoContent("There is no plugin to display here now", ""));
            return;
        }
        PluginDetails details = new PluginDetails();
        PluginList pluginListDiv = new PluginList(pluginProxy, handler, details);
        SplitLayout splitLayout = new SplitLayout(pluginListDiv, details, SplitLayout.Orientation.HORIZONTAL);
        splitLayout.setSplitterPosition(47);
        splitLayout.setSizeFull();
        add(splitLayout);
    }
}
