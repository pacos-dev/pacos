package org.pacos.core.component.variable.view;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.proxy.UserVariableCollectionProxy;
import org.pacos.core.component.variable.proxy.UserVariableProxy;
import org.pacos.core.component.variable.view.config.VariableConfig;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class PanelVariableTest {

    @Test
    void whenInitializeThenNoException() {
        VaadinMock.mockSystem();
        UserVariableCollectionProxy userVariableCollectionProxy = mock(UserVariableCollectionProxy.class);
        UserVariableProxy userVariableProxy = mock(UserVariableProxy.class);

        assertDoesNotThrow(() -> new PanelVariable(new VariableConfig(), userVariableCollectionProxy, userVariableProxy));
    }
}