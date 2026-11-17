package org.pacos.core.component.variable.view.user;

import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserCollectionContextMenuTest {

    @Test
    void whenInitializedThenContextMenuIsRegisteredForGrid() {
        VaadinMock.mockSystem();
        UserVariableCollectionGrid grid = mock(UserVariableCollectionGrid.class);
        UserVariableSystem system = mock(UserVariableSystem.class);
        GridContextMenu<UserVariableCollectionDTO> contextMenu = new GridContextMenu<>(grid);
        when(grid.addContextMenu()).thenReturn(contextMenu);

        assertDoesNotThrow(() -> new UserCollectionContextMenu(grid, system));

        verify(grid).addContextMenu();
    }
}