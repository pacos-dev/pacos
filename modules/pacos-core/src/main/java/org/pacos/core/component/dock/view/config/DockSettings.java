package org.pacos.core.component.dock.view.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import com.vaadin.flow.component.html.Hr;

/**
 * Dock setting panel displayed on Settings window
 */
public class DockSettings extends SettingPageLayout {

    private static final String DESCRIPTION =
            "Selected applications and modules will be pinned to the dock by default for new user accounts.";
    private final RegistryProxy registryProxy;
    protected final DivUtils appIconContent;
    private List<String> activatorConfiguration;

    public DockSettings(RegistryProxy registryProxy) {
        this.registryProxy = registryProxy;
        add(new InfoBox(DESCRIPTION));
        this.appIconContent = new DivUtils();
        add(appIconContent);

        Optional<ActivatorConfig> config = registryProxy
                .readRegistry(RegistryName.DEFAULT_DOCK_CONFIG, ActivatorConfig.class);
        this.activatorConfiguration = new ArrayList<>();
        config.ifPresent(activatorConfig -> activatorConfiguration = activatorConfig.activators());

        for (WindowConfig moduleConfig : PluginResource.getAllWindowConfig().stream().filter(WindowConfig::isApplication).toList()) {
            final CheckboxUtils checkbox = new CheckboxUtils(moduleConfig.title());
            appIconContent.withComponents(createIconWithCheckbox(moduleConfig, checkbox));
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
        add(new ButtonUtils("Save configuration", e -> saveConfiguration()).primaryLayout());
    }

    private static DivUtils createIconWithCheckbox(WindowConfig moduleConfig, CheckboxUtils checkbox) {
        return new DivUtils().withStyle("display", "inline-block")
                .withStyle("width", "300px")
                .withComponents(
                        new ImageUtils(moduleConfig.icon(), moduleConfig.title()).withStyle("float", "left")
                                .withWidth(30)
                                .withHeight(30),
                        checkbox
                                .withStyle("float", "left").withStyle("margin-top", "10px"));
    }

    private void saveConfiguration() {
        registryProxy.saveRegistry(RegistryName.DEFAULT_DOCK_CONFIG, new ActivatorConfig(activatorConfiguration));
        NotificationUtils.success("Saved");
    }

    public static String getSearchIndex() {
        return DESCRIPTION;
    }

    @Override
    public void onShortCutDetected(ShortcutType shortcutType) {
        if (shortcutType.isSave()) {
            saveConfiguration();
        }
    }
}
