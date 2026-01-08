package org.pacos.core.component.security;

import lombok.Getter;
import org.pacos.base.security.Permission;

@Getter
public enum SystemPermissions implements Permission {
    SETTINGS_PLUGIN_VISIBLE("setting.plugin.visible",
            "Settings plugin visible",
            PermissionCategory.PLUGIN.getName(),
            "Setting plugin will be visible in applications"),
    PLUGIN_AUTO_UPDATE("setting.plugin.auto-update.enable",
            "Enable plugin auto update",
            PermissionCategory.SYSTEM.getName(),
            "Allow enabling automatic plugin updates"),
    API_TAB_VISIBLE("setting.tab.api.visible",
            "Settings API tab visible",
            PermissionCategory.SETTINGS.getName(),
            "API tab will be visible in setting plugin"),
    SYSTEM_RESTART("system.restart",
            "Application restart",
            PermissionCategory.SYSTEM.getName(),
            "Allow to restart application from system setting tab "),
    APP_STORE_VISIBLE("setting.tab.app-store.visible",
            "Settings app store tab visible",
            PermissionCategory.PLUGIN.getName(),
            "App store tab will be visible in setting plugin"),
    PLUGIN_MANAGEMENT_TAB_VISIBLE("setting.tab.plugin-management.visible",
            "Settings plugin management tab visible",
            PermissionCategory.SETTINGS.getName(),
            "Plugin management tab will be visible in setting plugin"),
    DEFAULT_PERMISSIONS_TAB_VISIBLE("setting.tab.default.permissions.visible",
            "Settings permissions tab visible",
            PermissionCategory.SETTINGS.getName(),
            "Permissions tab will be visible in setting plugin"),
    USER_PERMISSIONS_TAB_VISIBLE("setting.tab.user.permissions.visible",
            "Settings user permissions tab visible",
            PermissionCategory.SETTINGS.getName(),
            "Permissions tab will be visible in setting plugin"),
    SYSTEM_AUTO_UPDATE("system.auto-update.enable",
            "Enable system auto update",
            PermissionCategory.SYSTEM.getName(),
            "Allow enabling automatic system updates"),
    SYSTEM_ACCESS_CONFIGURATION("system.access.configuration",
            "Configure system access",
            PermissionCategory.SYSTEM.getName(),
            "Allow managing guest mode and registration panel availability"),
    SYSTEM_BACKGROUND_CONFIGURATION("system.background", "Configure background",
            PermissionCategory.SYSTEM.getName(), "Change desktop background"),
    SYSTEM_ACCESS_TAB_VISIBLE("setting.tab.system-access.visible",
            "Settings system access tab visible",
            PermissionCategory.SETTINGS.getName(),
            "System access tab will be visible in setting plugin");

    private final String key;
    private final String label;
    private final String category;
    private final String description;

    SystemPermissions(String key, String label, String category, String description) {
        this.key = key;
        this.label = label;
        this.category = category;
        this.description = description;
    }

}
