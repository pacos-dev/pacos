package org.pacos.core.system.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.icon.VaadinIcon;

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
        assertEquals(VaadinIcon.MOON, UITheme.DARK.getIcon());
    }

    @Test
    void whenCalledGetIconForLightThenReturnSunIcon() {
        assertEquals(VaadinIcon.SUN, UITheme.LIGHT.getIcon());
    }
}
