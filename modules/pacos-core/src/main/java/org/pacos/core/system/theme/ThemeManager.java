package org.pacos.core.system.theme;

import java.util.Objects;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;

/**
 * Change app theme for current user session
 */
public class ThemeManager {

    private ThemeManager() {

    }

    /** Toggles the active theme and updates the supplied theme button icon. */
    public static void changeTheme(Button button) {
        String current = UI.getCurrent().getElement().getAttribute("theme");

        UITheme next = Objects.equals(current, UITheme.DARK.getThemeName()) ? UITheme.LIGHT : UITheme.DARK;
        button.setIcon(next.getIcon().create());
        setTheme(next);
    }

    /** Applies the given theme to the current Vaadin UI. */
    public static void setTheme(UITheme theme) {
        UI.getCurrent().getElement().setAttribute("theme", theme.getThemeName());
    }
}
