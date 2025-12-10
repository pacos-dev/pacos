package org.pacos.core.component.variable.view.help;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.view.config.VariablePluginHelpConfig;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class VariablePluginHelpTest {

    @Test
    void whenInitializeThenNoException() {
        VaadinMock.mockSystem();

        assertDoesNotThrow(() -> new VariablePluginHelp(new VariablePluginHelpConfig()));
    }
}