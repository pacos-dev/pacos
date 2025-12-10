package org.pacos.core.system.theme;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.dom.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ThemeManagerTest {

    @Test
    void whenThemeIsDarkThenChangeToLight(){
        UI mockUi = mock(UI.class);
        UI.setCurrent(mockUi);
        when(mockUi.getElement()).thenReturn(mock(Element.class));
        when(mockUi.getElement().getAttribute("theme")).thenReturn("dark");
        Button button = new Button(VaadinIcon.MOON_O.create());
        //when
        ThemeManager.changeTheme(button);
        //then
        assertEquals(VaadinIcon.SUN_O.create().getIcon(),((Icon)button.getIcon()).getIcon());
        verify(mockUi.getElement()).setAttribute("theme","light");
    }

    @Test
    void whenThemeIsLightThenChangeToDark(){
        UI mockUi = mock(UI.class);
        UI.setCurrent(mockUi);
        when(mockUi.getElement()).thenReturn(mock(Element.class));
        when(mockUi.getElement().getAttribute("theme")).thenReturn("light");
        Button button = new Button(VaadinIcon.SUN_O.create());
        //when
        ThemeManager.changeTheme(button);
        //then
        assertEquals(VaadinIcon.MOON_O.create().getIcon(),((Icon)button.getIcon()).getIcon());
        verify(mockUi.getElement()).setAttribute("theme","dark");
    }
}