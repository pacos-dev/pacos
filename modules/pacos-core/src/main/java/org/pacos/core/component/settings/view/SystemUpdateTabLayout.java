package org.pacos.core.component.settings.view;

import java.util.Optional;

import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.component.CheckboxUtils;
import org.pacos.base.utils.component.InfoBox;
import org.pacos.base.utils.component.ListContent;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.plugin.event.SystemRestartRequiredEvent;
import org.pacos.core.component.plugin.service.data.PluginUpdateResult;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.system.event.RestartSystemEvent;
import org.pacos.core.system.service.AutoUpdateService;
import org.pacos.core.system.service.data.SystemUpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemUpdateTabLayout extends SettingPageLayout {

    private static final Logger LOG = LoggerFactory.getLogger(SystemUpdateTabLayout.class);
    private final RegistryProxy registryProxy;
    private final ListContent infoContent;
    private final ButtonUtils updateSystemBtn;
    private final ButtonUtils updatePluginBtn;
    private final AutoUpdateService autoUpdateService;

    public SystemUpdateTabLayout(RegistryProxy registryProxy, AutoUpdateService autoUpdateService) {
        this.registryProxy = registryProxy;
        this.autoUpdateService = autoUpdateService;
        CheckboxUtils autoUpdateEnabled = new CheckboxUtils("Enable automatic system update. " +
                "In case of an update, there will be an automatic restart to reload the system configuration.");
        autoUpdateEnabled.withEnabledForPermission(SystemPermissions.SYSTEM_AUTO_UPDATE);
        autoUpdateEnabled.setValue(registryProxy.readBoolean(RegistryName.AUTO_UPDATE_ENABLED, false));
        autoUpdateEnabled.addValueChangeListener(e ->
                saveRegistryValue(RegistryName.AUTO_UPDATE_ENABLED, e.getValue()));

        CheckboxUtils autoUpdatePluginEnabled = new CheckboxUtils("Enable automatic plugin update");
        autoUpdatePluginEnabled.withEnabledForPermission(SystemPermissions.PLUGIN_AUTO_UPDATE);
        autoUpdatePluginEnabled.setValue(registryProxy.readBoolean(RegistryName.AUTO_UPDATE_PLUGIN_ENABLED, false));
        autoUpdatePluginEnabled.addValueChangeListener(e ->
                saveRegistryValue(RegistryName.AUTO_UPDATE_PLUGIN_ENABLED, e.getValue()));

        this.infoContent = new ListContent();
        this.updateSystemBtn = new ButtonUtils("Update system now and restart")
                .withEnabledForPermission(SystemPermissions.SYSTEM_RESTART)
                .floatRight()
                .primaryLayout()
                .withClickListener(e -> updateSystemBtnClickEvent());
        this.updatePluginBtn = new ButtonUtils("Update plugins")
                .withEnabledForPermission(SystemPermissions.SYSTEM_AUTO_UPDATE)
                .floatRight()
                .primaryLayout()
                .withClickListener(e -> updatePluginBtnClickEvent());

        ButtonUtils checkUpdateBtn = new ButtonUtils("Check available updates")
                .floatRight()
                .withClickListener(e -> checkUpdateBtnClickEvent());

        ButtonUtils restartApplication = new ButtonUtils("Restart Coupler")
                .withVisibleForPermission(SystemPermissions.SYSTEM_RESTART)
                .floatRight()
                .withClickListener(e -> RestartSystemEvent.fireEvent());

        add(new InfoBox("System update. Starts automatically at 2:00."));
        add(autoUpdateEnabled);
        add(autoUpdatePluginEnabled);
        add(new Hr());
        add(infoContent);
        add(new Hr());
        add(new HorizontalLayout(updateSystemBtn, updatePluginBtn, checkUpdateBtn));
        add(new Hr());
        add(restartApplication);
        refreshContent();

    }

    void checkUpdateBtnClickEvent() {
        boolean isToUpdate = autoUpdateService.checkIfUpdateAvailable();
        if (isToUpdate) {
            UISystem.getCurrent().notify(ModuleEvent.UPDATE_AVAILABLE);
        } else {
            NotificationUtils.info("No updates found");
        }
        refreshContent();
    }

    void updatePluginBtnClickEvent() {
        Optional<PluginUpdateResult> result = autoUpdateService.updatePlugins(true);
        if (result.isPresent()) {
            if (!result.get().updated().isEmpty()) {
                NotificationUtils.info("Updated " + result.get().updated().size() + " plugin");
                updatePluginBtn.setVisible(false);
            }
            if (!result.get().failed().isEmpty()) {
                NotificationUtils.error("Failed to updated " + result.get().updated().size() + " plugin");
            }
            refreshInfoContent(registryProxy, infoContent);
        }

    }

    void updateSystemBtnClickEvent() {
        Optional<SystemUpdateResult> result = autoUpdateService.updateSystem(true);
        if (result.isEmpty() || !result.get().isToUpdate()) {
            NotificationUtils.info("No updates found");
            refreshInfoContent(registryProxy, infoContent);
        } else {
            LOG.info("System updated. Restarting....");
            SystemRestartRequiredEvent.fireEvent(registryProxy);
            RestartSystemEvent.fireEvent();
        }
    }

    private static void refreshInfoContent(RegistryProxy registryProxy, ListContent infoContent) {
        String systemVersion = registryProxy.readRegistryOrDefault(RegistryName.SYSTEM_VERSION, "1.0");
        String lastUpdated = registryProxy.readTime(RegistryName.LAST_UPDATE_TIME, "---");
        String lastChecked = registryProxy.readTime(RegistryName.LAST_UPDATE_CHECK_TIME, "---");

        String availableVersion = registryProxy.isSystemToUpdate() ?
                registryProxy.readRegistryOrDefault(RegistryName.AVAILABLE_SYSTEM_VERSION,
                        "---") : "no update";
        String availablePlugin =
                registryProxy.readRegistry(RegistryName.AVAILABLE_PLUGINS_COUNT_TO_UPDATE, Integer.class, 0) > 0 ?
                        registryProxy.readRegistryOrDefault(RegistryName.AVAILABLE_PLUGINS_COUNT_TO_UPDATE,
                                "---") : "no update";

        infoContent.removeAll();
        infoContent.addRow("Last updated", lastUpdated, false);
        infoContent.addRow("Last checked", lastChecked, false);
        infoContent.addRow("Current system version", systemVersion, false);
        infoContent.addRow("Available system version", availableVersion, true);
        infoContent.addRow("Available plugins", availablePlugin, true);
    }

    private void refreshContent() {
        int pluginsToUpdateCount =
                registryProxy.readRegistry(RegistryName.AVAILABLE_PLUGINS_COUNT_TO_UPDATE, Integer.class, 0);
        boolean systemToUpdate = registryProxy.isSystemToUpdate();
        boolean pluginsToUpdate = pluginsToUpdateCount > 0;

        updateSystemBtn.setVisible(systemToUpdate);
        updatePluginBtn.setVisible(pluginsToUpdate);

        refreshInfoContent(registryProxy, infoContent);
    }

    void saveRegistryValue(RegistryName registryName, boolean value) {
        registryProxy.saveRegistry(registryName, value);
        String result = value ? "Enable " : "Disable ";
        NotificationUtils.success(result + "auto update");
    }

    @Override
    public void onShortCutDetected(ShortcutType shortcutType) {
        //shortcut listener not implemented
    }
}