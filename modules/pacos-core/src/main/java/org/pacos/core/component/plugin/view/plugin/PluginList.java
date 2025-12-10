package org.pacos.core.component.plugin.view.plugin;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.plugin.service.PluginDownloadState;

public class PluginList extends Div {

    private final transient PluginProxy pluginProxy;
    private final transient PluginHandler pluginHandler;
    private final Map<PluginDTO, Button> btnMap = new HashMap<>();
    private final PluginDetails details;

    PluginList(PluginProxy pluginProxy, PluginHandler pluginHandler, PluginDetails details) {
        this.pluginProxy = pluginProxy;
        this.pluginHandler = pluginHandler;
        this.details = details;
        setClassName("m-list");

        for (PluginDTO pluginDTO : pluginHandler.getPluginToDisplay()) {
            PluginRowLine row = new PluginRowLine(pluginDTO);
            Button button = configureForMarketPlace(pluginDTO, row);
            btnMap.put(pluginDTO, button);
            row.addClickListener(e -> onPluginRowClickEvent(details, pluginDTO, button));
            add(row);
        }
        UISystem.getCurrent().subscribeOnAttached(this, ModuleEvent.PLUGIN_DOWNLOAD_STATE_CHANGED, this::downloadStateNotifier);
        UISystem.getCurrent().subscribeOnAttached(this, ModuleEvent.PLUGIN_INSTALLED, this::installedNotifier);
    }

    private void downloadStateNotifier(Object event) {
        PluginDownloadState status = (PluginDownloadState) event;
        Button btn = btnMap.get(status.pluginDTO());
        setBtnStatus(status.pluginDTO(), btn, details, status.status());
    }

    private void installedNotifier(Object event) {
        PluginDTO pluginDTO = (PluginDTO) event;
        Button btn = btnMap.get(pluginDTO);
        if(btn==null) {
            return;
        }
        setBtnStatus(pluginDTO, btn, details, DownloadPluginStatus.FINISHED);
    }

    private Button configureForMarketPlace(PluginDTO pluginInMarket, PluginRowLine row) {
        Button button;
        button = new Button();
        if (pluginHandler.isInstalled(pluginInMarket)) {
            if (pluginHandler.isToUpdate(pluginInMarket)) {
                button.setText("Update");
                button.addClickListener(e -> installPlugin(pluginInMarket));
            } else {
                button.setText("Installed");
                button.setEnabled(false);
            }
        } else {
            button.setText("Install");
            button.addClickListener(e -> installPlugin(pluginInMarket));
        }

        button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        button.addClassName("btn-group");
        row.add(button);
        return button;
    }

    static void onPluginRowClickEvent(PluginDetails details, PluginDTO pluginDTO, Button button) {
        details.showForPlugin(pluginDTO);
        details.setBtnAndSynchronize(button);
    }


    void installPlugin(PluginDTO pluginDTO) {
        pluginProxy.getPluginInstallService().downloadAndInstallPluginFromRemote(pluginDTO, pluginHandler.repository());
    }

    static void setBtnStatus(PluginDTO pluginDTO, Button btn, PluginDetails details, DownloadPluginStatus status) {
        switch (status) {
            case DOWNLOADING -> {
                btn.setTooltipText(null);
                btn.setEnabled(false);
                btn.setText("Downloading");
                btn.getStyle().set("color", "var(--lumo-disabled-text-color)");
                btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                btn.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
            }
            case INSTALLING -> {
                btn.setText("Installing ...");
                btn.setTooltipText("The plugin is started");
                btn.getStyle().set("color", "var(--lumo-disabled-text-color)");
            }
            case ERROR -> {
                btn.setText("Error. Try again");
                btn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                btn.addThemeVariants(ButtonVariant.LUMO_ERROR);
                btn.setTooltipText(pluginDTO.getErrMsg());
                btn.setEnabled(true);
            }
            case FINISHED -> {
                btn.setText("Installed");
                btn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                btn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                btn.setEnabled(false);
            }
        }
        details.setBtnAndSynchronize(btn);
    }
}
