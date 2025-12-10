package org.pacos.core.system.theme;

import com.vaadin.flow.component.icon.VaadinIcon;

public enum UITheme {
    DARK("dark", VaadinIcon.MOON_O),
    LIGHT("light", VaadinIcon.SUN_O);

    private final String themeName;
    private final VaadinIcon icon;

    UITheme(String themeName, VaadinIcon icon) {
        this.themeName = themeName;
        this.icon = icon;
    }

    public String getThemeName() {
        return themeName;
    }

    public VaadinIcon getIcon() {
        return icon;
    }
}
