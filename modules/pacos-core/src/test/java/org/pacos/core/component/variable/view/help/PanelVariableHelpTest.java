package org.pacos.core.component.variable.view.help;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.view.config.VariableHelpConfig;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PanelVariableHelpTest {

    @Test
    void whenInitializeThenNoException() {
        VaadinMock.mockSystem();

        assertDoesNotThrow(() -> new PanelVariableHelp(new VariableHelpConfig()));
    }
}