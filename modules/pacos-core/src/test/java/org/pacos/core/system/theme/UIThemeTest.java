package org.pacos.core.system.theme;

import com.vaadin.flow.component.icon.VaadinIcon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UIThemeTest {

    @Test
    void whenCalledGetThemeNameForDarkThenReturnDark() {
        assertEquals("dark", UITheme.DARK.getThemeName());
    }

    @Test
    void whenCalledGetThemeNameForLightThenReturnLight() {
        assertEquals("light", UITheme.LIGHT.getThemeName());
    }

    @Test
    void whenCalledGetIconForDarkThenReturnMoonIcon() {
        assertEquals(VaadinIcon.MOON_O, UITheme.DARK.getIcon());
    }

    @Test
    void whenCalledGetIconForLightThenReturnSunIcon() {
        assertEquals(VaadinIcon.SUN_O, UITheme.LIGHT.getIcon());
    }
}
