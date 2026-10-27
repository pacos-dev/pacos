package org.pacos.core.component.variable.view.user;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class UserCollectionContextMenuTest {

    private UserVariableSystem system;
    private UserVariableCollectionGrid grid;

    @BeforeEach
    void setUp() {
        VaadinMock.mockSystem();
        system = mock(UserVariableSystem.class);
        grid = mock(UserVariableCollectionGrid.class);
    }

    @Test
    void whenContextMenuIsConstructedThenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> new UserCollectionContextMenu(grid, system));
    }

    @Test
    void whenGlobalCollectionIsNotEditableThenEditActionDoesNotOpenEditor() {
        UserVariableCollectionDTO global = UserVariableCollectionDTO.builder().global(true).name("Global").build();
        Mockito.when(grid.getSelectedCollection()).thenReturn(java.util.Optional.of(global));

        UserCollectionContextMenu contextMenu = new UserCollectionContextMenu(grid, system);
        assertDoesNotThrow(() -> contextMenu.getClass());
        verify(grid, never()).editItem(global);
    }
}
