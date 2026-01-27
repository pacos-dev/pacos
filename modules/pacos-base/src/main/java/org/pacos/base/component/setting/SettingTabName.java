package org.pacos.base.component.setting;

public enum SettingTabName {
    PLUGINS("Plugins"),
    DOCK("Dock"),
    ACCESS_MANAGEMENT("Access management"),
    USER("Users"),
    SYSTEM("System"),
    SYSTEM_UPDATE("Update"),
    SYSTEM_ACCESS("Guest & Registration"),
    SYSTEM_BACKGROUND("Background"),
    RESTART("Restart"),
    API("API access"),
    ROLES("Roles & Permissions"),
    ONBOARDING("Onboarding / Defaults");

    private final String name;

    SettingTabName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
