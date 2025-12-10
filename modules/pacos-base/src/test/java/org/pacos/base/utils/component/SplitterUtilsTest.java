package org.pacos.base.utils.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.Theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SplitterUtilsTest {

    @Test
    void whenConstructorWithComponentsCalledThenSplitterIsFullSize() {
        Component primary = new Div();
        Component secondary = new Div();

        SplitterUtils splitter = new SplitterUtils(primary, secondary);

        assertEquals("100%",splitter.getWidth());
        assertEquals("100%",splitter.getHeight());
    }

    @Test
    void whenConstructorWithPositionCalledThenSplitterHasCorrectPosition() {
        Component primary = new Div();
        Component secondary = new Div();
        double position = 0.5;

        SplitterUtils splitter = new SplitterUtils(primary, secondary, position);

        assertEquals(position, splitter.getSplitterPosition());
        assertEquals(SplitLayout.Orientation.VERTICAL, splitter.getOrientation());
        assertTrue(splitter.getThemeNames().contains(Theme.APP_STYLE.getName()));
        assertTrue(splitter.getThemeNames().contains(Theme.SMALL.getName()));
    }

    @Test
    void whenOrientationCalledThenSplitterOrientationIsSet() {
        Component primary = new Div();
        Component secondary = new Div();
        SplitterUtils splitter = new SplitterUtils(primary, secondary);

        splitter.orientation(SplitLayout.Orientation.HORIZONTAL);

        assertEquals(SplitLayout.Orientation.HORIZONTAL, splitter.getOrientation());
    }

    @Test
    void whenPositionCalledThenSplitterPositionIsSet() {
        Component primary = new Div();
        Component secondary = new Div();
        double position = 0.75;

        SplitterUtils splitter = new SplitterUtils(primary, secondary);
        splitter.position(position);

        assertEquals(position, splitter.getSplitterPosition());
    }

    @Test
    void whenThemeCalledThenThemesAreAdded() {
        Component primary = new Div();
        Component secondary = new Div();
        SplitterUtils splitter = new SplitterUtils(primary, secondary);

        splitter.theme(Theme.APP_STYLE, Theme.SMALL);

        assertTrue(splitter.getThemeNames().contains(Theme.APP_STYLE.getName()));
        assertTrue(splitter.getThemeNames().contains(Theme.SMALL.getName()));
    }

    @Test
    void whenLockedCalledThenLockedThemeIsAdded() {
        Component primary = new Div();
        Component secondary = new Div();
        SplitterUtils splitter = new SplitterUtils(primary, secondary);

        splitter.locked();

        assertTrue(splitter.getThemeNames().contains("locked"));
    }
}
