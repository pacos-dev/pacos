package org.pacos.core.component.plugin.view.tab;

import java.nio.file.Path;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.pacos.base.component.Color;
import org.pacos.base.component.Spinner;
import org.pacos.base.event.UISystem;
import org.pacos.base.file.FileInfo;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.component.CheckboxUtils;
import org.pacos.base.utils.component.IconUtils;
import org.pacos.base.utils.component.ImageUtils;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.config.property.WorkingDir;
import org.pacos.core.component.plugin.PluginPermissions;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.event.RemovePluginEvent;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;
import org.pacos.core.component.plugin.proxy.PluginProxy;

public class PluginManagementGrid extends Grid<PluginDTO> {

    private final PluginProxy pluginProxy;
    private final PluginManager pluginManager;

    public PluginManagementGrid(PluginProxy pluginProxy, PluginManager pluginManager) {
        super(PluginDTO.class, false);
        this.pluginManager = pluginManager;
        this.pluginProxy = pluginProxy;
        addColumn(new ComponentRenderer<>(this::getImage));
        addColumn(PluginDTO::getName).setHeader("Name");
        addColumn(PluginDTO::getVersion).setHeader("Version");
        addColumn(new ComponentRenderer<>(plugin -> createCheckboxForAutoStartConfig(pluginProxy, plugin))).
                setHeader("Automatic startup ");
        addColumn(new ComponentRenderer<>(plugin ->
                getStatusIcon(PluginState.getState(plugin)))).setHeader("Status");
        addColumn(new ComponentRenderer<>(plugin -> createStartStopButtonForPlugin(plugin,PluginState.getState(plugin))))
                .setHeader("Start/Stop");
        addColumn(new ComponentRenderer<>(this::createLogsButton))
                .setHeader("Logs");
        addColumn(new ComponentRenderer<>(this::creatUninstallButtonForPlugin))
                .setHeader("Uninstall");

        setItems(PluginState.getPlugins());
    }

    static Checkbox createCheckboxForAutoStartConfig(PluginProxy pluginProxy, PluginDTO plugin) {
        Checkbox cb = new CheckboxUtils().withEnabledForPermission(PluginPermissions.CONFIGURE_AUTORUN);
        cb.setValue(!plugin.isDisabled());
        cb.addValueChangeListener(e -> updatePluginAutoStart(pluginProxy, plugin, e.getValue()));
        return cb;
    }

    private static void updatePluginAutoStart(PluginProxy pluginProxy, PluginDTO plugin, boolean value) {
        if (value) {
            pluginProxy.getPluginService().enablePlugin(plugin);
        } else {
            pluginProxy.getPluginService().disablePlugin(plugin);
        }
        NotificationUtils.success("Updated");
    }

    protected Image getImage(PluginDTO plugin) {
        Image image;
        if (plugin.getIconAbsolutePath().toFile().exists()) {
            image = ImageUtils.fromLocation(plugin.getIconAbsolutePath(), plugin.getArtifactName());
        } else {
            image = new Image();
        }
        image.setWidth("32px");
        image.setHeight("32px");
        return image;
    }

    protected static Component getStatusIcon(PluginStatusEnum state) {
        return switch (state) {
            case ON -> IconUtils.colorIcon(VaadinIcon.PLAY_CIRCLE, Color.GREEN, "Plugin is running");
            case OFF -> IconUtils.colorIcon(VaadinIcon.STOP, Color.RED, "Plugin is disabled");
            case ERROR ->
                    IconUtils.colorIcon(VaadinIcon.REPLY, Color.RED, "An error occurred while starting the plugin");
            default -> new Spinner();
        };
    }

    Button createStartStopButtonForPlugin(PluginDTO plugin, PluginStatusEnum status) {
        ButtonUtils button =
                new ButtonUtils(getButtonText(status));
        button.addClickListener(e -> startOrStopBtnEvent(plugin, button));
        button.setEnabled(
                status.canProcess() && UserSession.getCurrent().hasPermission(PluginPermissions.LIFECYCLE_PLUGIN));
        button.withEnabledForPermission(PluginPermissions.LIFECYCLE_PLUGIN);
        return button;
    }

    void startOrStopBtnEvent(PluginDTO plugin, ButtonUtils button) {
        if (PluginState.getState(plugin).canRun()) {
            button.setEnabled(false);
            button.setText("Waiting");
            pluginManager.startPlugin(plugin);
        } else {
            button.setEnabled(false);
            button.setText("Waiting");
            pluginManager.stopPlugin(plugin);
        }
    }

    Button creatUninstallButtonForPlugin(PluginDTO plugin) {
        Button button = new ButtonUtils(VaadinIcon.TRASH.create()).errorLayout()
                .withEnabledForPermission(PluginPermissions.REMOVE_PLUGIN);
        button.addClickListener(
                e -> RemovePluginEvent.fireEvent(pluginProxy, plugin,
                () -> getUI().ifPresent(ui ->
                ui.access(() -> {
                    setItems(PluginState.getPlugins());
                    NotificationUtils.success("Uninstalled");
                    ui.push();
                }))));
        return button;
    }

    private Button createLogsButton(PluginDTO plugin) {
        Button button = new ButtonUtils(VaadinIcon.FILE_TEXT_O.create()).infoLayout();
        Path logPath = WorkingDir.getModulePath(plugin.getArtifactName()).resolve("log")
                .resolve("plugin_initialization.log");
        button.setTooltipText(logPath.toString());
        button.addClickListener(e -> {

            if (logPath.toFile().exists()) {
                UISystem.getCurrent().getApplicationManager().open(FileInfo.of(logPath));
            } else {
                NotificationUtils.info("Log file from plugin initialization not found");
            }
        });
        return button;
    }

    protected static String getButtonText(PluginStatusEnum state) {
        return switch (state) {
            case ON -> "Turn off";
            case OFF -> "Turn on";
            case ERROR -> "Error. Try again";
            case SHUTDOWN -> "Stopping";
            case INITIALIZATION -> "Initializing";
        };
    }

    void refreshPluginRow(PluginDTO pluginDTO) {
        if (this.getDataCommunicator().isItemActive(pluginDTO)) {
            this.getDataProvider().refreshItem(pluginDTO);
        } else {
            this.setItems(PluginState.getPlugins());
        }
    }
}
