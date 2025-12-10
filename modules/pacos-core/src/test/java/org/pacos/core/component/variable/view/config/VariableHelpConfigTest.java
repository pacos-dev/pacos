package org.pacos.core.component.variable.view.config;

import org.junit.jupiter.api.Test;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.core.component.variable.view.help.PanelVariableHelp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class VariableHelpConfigTest {

    private final VariableHelpConfig variableHelpConfig = new VariableHelpConfig();

    @Test
    void whenTitleThenReturnsVariableHelp() {
        assertEquals("Variable help", variableHelpConfig.title());
    }

    @Test
    void whenIconThenReturnsQuestionIconUrl() {
        assertEquals(PacosIcon.QUESTION.getUrl(), variableHelpConfig.icon());
    }

    @Test
    void whenActivatorClassThenReturnsPanelVariableHelpClass() {
        assertEquals(PanelVariableHelp.class, variableHelpConfig.activatorClass());
    }

    @Test
    void whenIsApplicationThenReturnsFalse() {
        assertFalse(variableHelpConfig.isApplication());
    }

    @Test
    void whenIsAllowMultipleInstanceThenReturnsFalse() {
        assertFalse(variableHelpConfig.isAllowMultipleInstance());
    }
}
