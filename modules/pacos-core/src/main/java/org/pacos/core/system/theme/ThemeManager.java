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

    public static void changeTheme(Button button) {
        String current = UI.getCurrent().getElement().getAttribute("theme");

        UITheme next = Objects.equals(current, UITheme.DARK.getThemeName()) ? UITheme.LIGHT : UITheme.DARK;
        button.setIcon(next.getIcon().create());
        setTheme(next);
    }

    public static void setTheme(UITheme theme) {
        UI.getCurrent().getElement().setAttribute("theme", theme.getThemeName());
    }
}
