package org.pacos.common.view.menu;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.IconFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.dom.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ModuleMenuBarTest {

    private ModuleMenuBar moduleMenuBar;
    private MenuElement menuElement;
    private MenuItem menuItem;

    @BeforeEach
    void setup() {
        moduleMenuBar = new ModuleMenuBar();
        menuElement = mock(MenuElement.class);
        menuItem = mock(MenuItem.class);
    }

    @Test
    void whenModifyEnabledThenMenuItemEnabledStateChanges() {
        moduleMenuBar.addMenuItem(menuElement, menuItem);
        moduleMenuBar.modifyEnabled(false, menuElement);

        verify(menuItem).setEnabled(false);
    }

    @Test
    void whenAddMenuItemWithComponentThenMenuItemStoredInMap() {
        Component component = new Span();
        when(menuElement.getDisplayName()).thenReturn("Test");
        MenuItem addedItem = moduleMenuBar.addMenuItem(menuElement, component, click -> {
        });
        assertEquals(addedItem, moduleMenuBar.getMenuItem(menuElement));
    }

    @Test
    void whenAddSpaceThenMenuItemIsAddedWithWidth() {
        moduleMenuBar.addSpace();
        MenuItem spaceItem = moduleMenuBar.getItems().get(moduleMenuBar.getItems().size() - 1);
        Element divElement = spaceItem.getElement();
        assertEquals("50px", divElement.getStyle().get("width"));
        assertFalse(spaceItem.isEnabled());
    }

    @Test
    void whenAddDividerThenMenuItemIsAddedWithDividerProperty() {
        moduleMenuBar.addDivider();
        MenuItem dividerItem = moduleMenuBar.getItems().get(moduleMenuBar.getItems().size() - 1);
        Element divElement = dividerItem.getElement();
        assertEquals("divider", divElement.getProperty("part"));
        assertFalse(dividerItem.isEnabled());
    }

    @Test
    void whenColorIconCalledThenIconHasColor() {
        IconFactory iconFactory = VaadinIcon.EDIT;
        Color color = Color.RED;
        var icon = ModuleMenuBar.colorIcon(iconFactory, color);
        assertEquals(color.getColor(), icon.getElement().getAttribute(Color.RED.name()));
    }
}
