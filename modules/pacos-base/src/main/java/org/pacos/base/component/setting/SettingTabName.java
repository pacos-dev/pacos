package org.pacos.base.component.setting;

public enum SettingTabName {
    PLUGINS("Plugins"),
    PERMISSIONS("Permissions"),
    SYSTEM("System"),
    SYSTEM_UPDATE("Update"),
    SYSTEM_ACCESS("Guest & Registration"),
    SYSTEM_BACKGROUND("Background"),
    RESTART("Restart"),
    API("API access"),
    USER_PERMISSION("User permission");

    private final String name;

    SettingTabName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
