package org.pacos.core.component.security;

public enum PermissionCategory {
    PLUGIN("plugin"),
    SYSTEM("system"),
    SETTINGS("plugin"),
    VARIABLE("variable"),
    API("api");
    private final String name;

    PermissionCategory(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
