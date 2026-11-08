package org.pacos.core.component.plugin.view.plugin;

import java.util.ArrayList;
import java.util.List;

import org.pacos.base.component.NoContent;
import org.pacos.base.component.Spinner;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.loader.PluginRefreshListener;
import org.pacos.core.component.plugin.proxy.PluginProxy;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.splitlayout.SplitLayout;

public class PluginListView extends Div implements PluginRefreshListener {

    private final transient PluginProxy pluginProxy;
    private final AppRepository repository;
    private final UI ui;
    private final boolean installerContext;

    public PluginListView(PluginProxy pluginProxy, boolean installerContext) {
        this(pluginProxy, AppRepository.pluginRepo(), installerContext);
    }

    public PluginListView(PluginProxy pluginProxy, AppRepository repository) {
        this(pluginProxy, repository, false);
    }

    private PluginListView(PluginProxy pluginProxy, AppRepository repository, boolean installerContext) {
        this.setSizeFull();
        this.pluginProxy = pluginProxy;
        this.repository = repository;
        this.installerContext = installerContext;
        add(new Spinner().center());
        this.ui = UI.getCurrent();
        getStyle().set("min-height", "0");
        getStyle().set("overflow-y", "auto");
    }

    @Override
    public void listLoaded(List<Plugin> pluginList) {
        List<PluginDTO> installedPlugins = pluginProxy.getPluginService().findAll();
        List<PluginDTO> pluginsFromRepo = new ArrayList<>();
        if (pluginList != null) {
            pluginsFromRepo = pluginList.stream().map(PluginDTO::new).toList();
        }

        PluginHandler handler = new PluginHandler(installedPlugins, pluginsFromRepo, repository);

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
        PluginDetails details = new PluginDetails(repository);
        PluginList pluginListDiv = new PluginList(pluginProxy, handler, details, installerContext);
        SplitLayout splitLayout = new SplitLayout(new Scroller(pluginListDiv), details, SplitLayout.Orientation.HORIZONTAL);
        splitLayout.setSplitterPosition(47);
        splitLayout.setSizeFull();
        add(splitLayout);
    }
}