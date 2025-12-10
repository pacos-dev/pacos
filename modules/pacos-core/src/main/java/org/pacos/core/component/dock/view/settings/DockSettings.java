package org.pacos.core.component.dock.view.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.html.Hr;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.component.CheckboxUtils;
import org.pacos.base.utils.component.DivUtils;
import org.pacos.base.utils.component.ImageUtils;
import org.pacos.base.utils.component.InfoBox;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.plugin.manager.PluginResource;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;

/**
 * Dock setting panel displayed on Settings window
 */
public class DockSettings extends SettingPageLayout {

    private static final String DESCRIPTION =
            "Marked applications/modules will be pinned to the dock for new user account";

    public DockSettings(RegistryProxy registryProxy) {
        add(new InfoBox(DESCRIPTION));
        DivUtils appSet = new DivUtils();
        add(appSet);

        Optional<ActivatorConfig> config = registryProxy
                .readRegistry(RegistryName.DEFAULT_DOCK_CONFIG, ActivatorConfig.class);
        List<String> activatorConfiguration;
        if (config.isPresent()) {
            activatorConfiguration = config.get().activators();
        } else {
            activatorConfiguration = new ArrayList<>();
        }

        for (WindowConfig moduleConfig : PluginResource.getAllWindowConfig().stream().filter(WindowConfig::isApplication).toList()) {
            final CheckboxUtils checkbox = new CheckboxUtils(moduleConfig.title());
            appSet.withComponents(new DivUtils().withStyle("display", "inline-block")
                    .withStyle("width", "300px")
                    .withComponents(
                            new ImageUtils(moduleConfig.icon(), moduleConfig.title()).withStyle("float", "left")
                                    .withWidth(40)
                                    .withHeight(40),
                            checkbox
                                    .withStyle("float", "left").withStyle("margin-top", "10px")));
            final String activatorName = moduleConfig.activatorClass().getSimpleName();
            checkbox.setValue(activatorConfiguration.contains(activatorName));
            checkbox.addValueChangeListener(e -> {
                if (e.getValue() != null && e.getValue()) {
                    activatorConfiguration.add(activatorName);
                } else {
                    activatorConfiguration.remove(activatorName);
                }
            });
        }
        add(new Hr());
        add(new ButtonUtils("Save configuration", e -> {
            registryProxy.saveRegistry(RegistryName.DEFAULT_DOCK_CONFIG, new ActivatorConfig(activatorConfiguration));
            NotificationUtils.success("Saved");
        }).primaryLayout());
    }

    @Override
    public void onShortCutDetected(ShortcutType shortcutType) {
        //not implemented
    }
}
