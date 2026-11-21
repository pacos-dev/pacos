package org.pacos.core.component.variable.view.user;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.event.user.CloneCollectionEvent;
import org.pacos.core.component.variable.event.user.RemoveCollectionEvent;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu.GridContextMenuItemClickEvent;
import com.vaadin.flow.data.selection.SingleSelect;

class UserCollectionContextMenuTest {

    private UserVariableSystem system;
    private UserVariableCollectionGrid grid;
    private GridContextMenu<UserVariableCollectionDTO> menu;
    private SingleSelect selection;

    @BeforeEach
    void setUp() {
        system = mock(UserVariableSystem.class);
        grid = mock(UserVariableCollectionGrid.class);
        menu = mock(GridContextMenu.class);
        selection = mock(SingleSelect.class);

        when(grid.addContextMenu()).thenReturn(menu);
        when(grid.asSingleSelect()).thenReturn(selection);
    }

    @Test
    void whenInitializedThenRegisterContextMenuItems() {
        assertDoesNotThrow(() -> new UserCollectionContextMenu(grid, system));

        verify(grid).addContextMenu();
        verify(menu).addGridContextMenuOpenedListener(any());
        verify(menu).addItem(eq("Open"), any(ComponentEventListener.class));
        verify(menu).addItem(eq("Clone"), any(ComponentEventListener.class));
        verify(menu).addItem(eq("Edit name"), any(ComponentEventListener.class));
        verify(menu).addComponent(any(com.vaadin.flow.component.html.Hr.class));
        verify(menu).addItem(any(Component.class), any(ComponentEventListener.class));
    }

    @Test
    void whenMenuIsOpenedWithoutItemThenCloseMenuAndClearSelection() {
        new UserCollectionContextMenu(grid, system);
        ArgumentCaptor<ComponentEventListener<GridContextMenu.GridContextMenuOpenedEvent<UserVariableCollectionDTO>>> captor =
                ArgumentCaptor.forClass(ComponentEventListener.class);
        verify(menu).addGridContextMenuOpenedListener(captor.capture());

        GridContextMenu.GridContextMenuOpenedEvent<UserVariableCollectionDTO> event =
                mock(GridContextMenu.GridContextMenuOpenedEvent.class);
        when(event.getItem()).thenReturn(Optional.empty());

        captor.getValue().onComponentEvent(event);

        verify(menu).close();
        verify(selection).setValue(null);
    }

    @Test
    void whenMenuIsOpenedWithItemThenSelectItem() {
        UserVariableCollectionDTO collection = UserVariableCollectionDTO.builder().id(10).name("Personal").build();
        new UserCollectionContextMenu(grid, system);
        ArgumentCaptor<ComponentEventListener<GridContextMenu.GridContextMenuOpenedEvent<UserVariableCollectionDTO>>> captor =
                ArgumentCaptor.forClass(ComponentEventListener.class);
        verify(menu).addGridContextMenuOpenedListener(captor.capture());

        GridContextMenu.GridContextMenuOpenedEvent<UserVariableCollectionDTO> event =
                mock(GridContextMenu.GridContextMenuOpenedEvent.class);
        when(event.getItem()).thenReturn(Optional.of(collection));

        captor.getValue().onComponentEvent(event);

        verify(selection).setValue(collection);
    }

    @Test
    void whenOpeningCollectionThenNotifySystemAndCloseMenu() {
        UserVariableCollectionDTO collection = UserVariableCollectionDTO.builder().id(12).name("Personal").build();
        new UserCollectionContextMenu(grid, system);
        ArgumentCaptor<ComponentEventListener<GridContextMenuItemClickEvent<UserVariableCollectionDTO>>> captor =
                ArgumentCaptor.forClass(ComponentEventListener.class);
        verify(menu).addItem(eq("Open"), captor.capture());

        GridContextMenuItemClickEvent<UserVariableCollectionDTO> event = mock(GridContextMenuItemClickEvent.class);
        when(event.getItem()).thenReturn(Optional.of(collection));

        captor.getValue().onComponentEvent(event);

        verify(system).notify(UserVariableEvent.OPEN_COLLECTION_VARIABLE_TAB, collection);
        verify(menu).close();
    }

    @Test
    void whenCloningCollectionThenFireCloneEventAndCloseMenu() {
        new UserCollectionContextMenu(grid, system);
        ArgumentCaptor<ComponentEventListener<GridContextMenuItemClickEvent<UserVariableCollectionDTO>>> captor =
                ArgumentCaptor.forClass(ComponentEventListener.class);
        verify(menu).addItem(eq("Clone"), captor.capture());

        GridContextMenuItemClickEvent<UserVariableCollectionDTO> event = mock(GridContextMenuItemClickEvent.class);
        when(event.getItem()).thenReturn(Optional.of(UserVariableCollectionDTO.builder().id(13).name("Personal").build()));

        try (var cloneEvent = mockStatic(CloneCollectionEvent.class)) {
            captor.getValue().onComponentEvent(event);

            cloneEvent.verify(() -> CloneCollectionEvent.fireEvent(system));
        }

        verify(menu).close();
    }

    @Test
    void whenEditingGlobalCollectionThenDoNotStartEditingAndCloseMenu() {
        UserVariableCollectionDTO collection = UserVariableCollectionDTO.builder().name("Global").global(true).build();
        new UserCollectionContextMenu(grid, system);
        ArgumentCaptor<ComponentEventListener<GridContextMenuItemClickEvent<UserVariableCollectionDTO>>> captor =
                ArgumentCaptor.forClass(ComponentEventListener.class);
        verify(menu).addItem(eq("Edit name"), captor.capture());

        GridContextMenuItemClickEvent<UserVariableCollectionDTO> event = mock(GridContextMenuItemClickEvent.class);
        when(event.getItem()).thenReturn(Optional.of(collection));

        captor.getValue().onComponentEvent(event);

        verify(grid, never()).editItem(collection);
        verify(menu).close();
    }

    @Test
    void whenEditingRegularCollectionThenStartEditingAndCloseMenu() {
        UserVariableCollectionDTO collection = UserVariableCollectionDTO.builder().id(11).name("Personal").build();
        new UserCollectionContextMenu(grid, system);
        ArgumentCaptor<ComponentEventListener<GridContextMenuItemClickEvent<UserVariableCollectionDTO>>> captor =
                ArgumentCaptor.forClass(ComponentEventListener.class);
        verify(menu).addItem(eq("Edit name"), captor.capture());

        GridContextMenuItemClickEvent<UserVariableCollectionDTO> event = mock(GridContextMenuItemClickEvent.class);
        when(event.getItem()).thenReturn(Optional.of(collection));

        captor.getValue().onComponentEvent(event);

        verify(grid).editItem(collection);
        verify(menu).close();
    }

    @Test
    void whenRemovingCollectionThenFireRemoveEventAndCloseMenu() {
        new UserCollectionContextMenu(grid, system);
        ArgumentCaptor<ComponentEventListener<GridContextMenuItemClickEvent<UserVariableCollectionDTO>>> captor =
                ArgumentCaptor.forClass(ComponentEventListener.class);
        verify(menu).addItem(any(Component.class), captor.capture());

        GridContextMenuItemClickEvent<UserVariableCollectionDTO> event = mock(GridContextMenuItemClickEvent.class);
        when(event.getItem()).thenReturn(Optional.of(UserVariableCollectionDTO.builder().id(14).name("Personal").build()));

        try (var removeEvent = mockStatic(RemoveCollectionEvent.class)) {
            captor.getValue().onComponentEvent(event);

            removeEvent.verify(() -> RemoveCollectionEvent.fireEvent(system));
        }

        verify(menu).close();
    }

    @Test
    void whenOpeningCollectionWithoutItemThenDoNotNotifySystemAndCloseMenu() {
        new UserCollectionContextMenu(grid, system);
        ArgumentCaptor<ComponentEventListener<GridContextMenuItemClickEvent<UserVariableCollectionDTO>>> captor =
                ArgumentCaptor.forClass(ComponentEventListener.class);
        verify(menu).addItem(eq("Open"), captor.capture());

        GridContextMenuItemClickEvent<UserVariableCollectionDTO> event = mock(GridContextMenuItemClickEvent.class);
        when(event.getItem()).thenReturn(Optional.empty());

        captor.getValue().onComponentEvent(event);

        verify(system, never()).notify(any(), any());
        verify(menu).close();
    }
}
