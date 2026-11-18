package org.pacos.core.component.variable.view.user;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.proxy.UserVariableCollectionProxy;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserCollectionContextMenuTest {

    @Test
    void whenInitializedThenContextMenuIsRegisteredForGrid() {
        VaadinMock.mockSystem();
        UserVariableSystem system = mock(UserVariableSystem.class);
        UserVariableCollectionProxy proxy = mock(UserVariableCollectionProxy.class);
        when(system.getUserVariableCollectionProxy()).thenReturn(proxy);
        when(proxy.loadUserCollections(2)).thenReturn(List.of());
        UserVariableCollectionGrid grid = spy(new UserVariableCollectionGrid(system));

        assertDoesNotThrow(() -> new UserCollectionContextMenu(grid, system));

        verify(grid).addContextMenu();
    }
}
