package org.pacos.common.view.menu.decorator;

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.dom.DomListenerRegistration;
import com.vaadin.flow.dom.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RetainOpenedMenuItemDecoratorTest {

    private MenuItem menuItem;
    private Element element;

    @BeforeEach
    void setup() {
        SubMenu subMenu = mock(SubMenu.class);
        menuItem = mock(MenuItem.class);
        element = mock(Element.class);
        when(menuItem.getElement()).thenReturn(element);

        when(menuItem.getElement().addEventListener(any(), any())).thenReturn(Mockito.mock(DomListenerRegistration.class));
        when(subMenu.getItems()).thenReturn(java.util.List.of(menuItem));
    }


    @Test
    void whenMenuItemCheckedThenCheckedAttributeIsSet() {
        when(menuItem.isCheckable()).thenReturn(true);
        when(menuItem.isChecked()).thenReturn(true);

        RetainOpenedMenuItemDecorator.onClickEvent(menuItem);
        verify(element).executeJs("this.setAttribute('" + RetainOpenedMenuItemDecorator.CHECKED_ATTR + "', '')");
    }

    @Test
    void whenMenuItemUncheckedThenCheckedAttributeIsRemoved() {
        when(menuItem.isCheckable()).thenReturn(true);
        when(menuItem.isChecked()).thenReturn(false);

        RetainOpenedMenuItemDecorator.onClickEvent(menuItem);
        verify(element).executeJs("this.removeAttribute('" + RetainOpenedMenuItemDecorator.CHECKED_ATTR + "')");
    }

    @Test
    void whenKeepOpenOnClickCalledWithSubMenuThenAllMenuItemsDecorated() {
        SubMenu subMenu = mock(SubMenu.class);
        MenuItem item1 = mock(MenuItem.class);
        MenuItem item2 = mock(MenuItem.class);
        when(item1.getElement()).thenReturn(mock(Element.class));
        when(item2.getElement()).thenReturn(mock(Element.class));
        when(item1.getElement().addEventListener(any(), any())).thenReturn(Mockito.mock(DomListenerRegistration.class));
        when(item2.getElement().addEventListener(any(), any())).thenReturn(Mockito.mock(DomListenerRegistration.class));
        when(subMenu.getItems()).thenReturn(java.util.List.of(item1, item2));

        RetainOpenedMenuItemDecorator.keepOpenOnClick(subMenu);

        verify(item1.getElement(), times(1)).addEventListener(eq("click"), any());
        verify(item2.getElement(), times(1)).addEventListener(eq("click"), any());
    }
}
