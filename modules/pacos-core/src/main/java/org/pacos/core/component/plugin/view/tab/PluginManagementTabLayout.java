package org.pacos.core.component.plugin.view.tab;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.details.Details;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.component.DivUtils;
import org.pacos.base.utils.component.InfoBox;
import org.pacos.base.utils.component.VerticalLayoutUtils;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.manager.data.PluginStatus;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.plugin.service.PluginDownloadState;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class PluginManagementTabLayout extends SettingPageLayout {

    private final PluginManagementGrid pluginsGrid;
    private final UserSession userSession;

    public PluginManagementTabLayout(PluginManager pluginManager, PluginProxy pluginProxy) {
        VerticalLayoutUtils.configure(this);
        Details details = new Details(new Text("Upload plugin"), new InstallPluginContentLayout(pluginManager, pluginProxy));
        details.setOpened(false);
        add(details);

        this.userSession = UserSession.getCurrent();
        this.pluginsGrid = new PluginManagementGrid(pluginProxy, pluginManager);

        UISystem.getCurrent().subscribeOnAttached(this, ModuleEvent.PLUGIN_INSTALL_STATE_CHANGED, this::pluginStatusChanged);
        UISystem.getCurrent().subscribeOnAttached(this, ModuleEvent.PLUGIN_DOWNLOAD_STATE_CHANGED, this::pluginDownloadedStatusChanged);

        InfoBox box = new InfoBox("Enable/disable a plugin in real time, " +
                "configure automatic start at system startup or upload plugin manually");

        Details details2 = new Details(new Text("Plugin management"), new DivUtils().withComponents(box, pluginsGrid));
        details2.setSizeFull();
        details2.setOpened(true);
        add(details2);
    }

    void pluginStatusChanged(Object event) {
        PluginStatus status = (PluginStatus) event;
        refreshItem(status.pluginDTO());
    }

    void pluginDownloadedStatusChanged(Object event) {
        PluginDownloadState status = (PluginDownloadState) event;
        refreshItem(status.pluginDTO());
    }

    void refreshItem(PluginDTO pluginDTO) {
        userSession.getUIs().forEach(ui -> ui.access(() -> {
            pluginsGrid.refreshPluginRow(pluginDTO);
            ui.push();
        }));
    }


    @Override
    public void onShortCutDetected(ShortcutType shortcutType) {
        //shortcut listener not implemented
    }
}
