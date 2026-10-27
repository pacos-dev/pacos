package org.pacos.core.component.variable.view.user;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pacos.base.event.UISystem;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.event.user.CloneCollectionEvent;
import org.pacos.core.component.variable.event.user.EditCollectionEvent;
import org.pacos.core.component.variable.event.user.RemoveCollectionEvent;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

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

        assertDoesNotThrow(() -> new UserCollectionContextMenu(grid, system));
        Mockito.verify(grid, Mockito.never()).editItem(global);
    }
}
