package org.pacos.core.component.plugin;

import lombok.Getter;
import org.pacos.base.security.Permission;
import org.pacos.core.component.security.PermissionCategory;

@Getter
public enum PluginPermissions implements Permission {

    LIFECYCLE_PLUGIN("setting.plugin.management.lifecycle",
            "Modify plugin state",
            PermissionCategory.PLUGIN.getName(),
            "Start or stop already installed plugins"),
    REMOVE_PLUGIN("setting.plugin.management.remove",
            "Remove installed plugins",
            PermissionCategory.PLUGIN.getName(),
            "Allow to uninstall already installed plugins"),
    INSTALL_PLUGIN("setting.plugin.management.install",
            "Install new plugins",
            PermissionCategory.PLUGIN.getName(),
            "Allow to upload and install new plugins"),
    CONFIGURE_AUTORUN("setting.plugin.management.autorun",
            "Configure plugin autorun",
            PermissionCategory.PLUGIN.getName(),
            "Allow to set plugins to run automatically after application start");

    private final String key;
    private final String label;
    private final String category;
    private final String description;

    PluginPermissions(String key, String label, String category, String description) {
        this.key = key;
        this.label = label;
        this.category = category;
        this.description = description;
    }

}

