package org.pacos.core.component.variable.view.user;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

}
