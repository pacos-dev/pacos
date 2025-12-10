package org.pacos.core.component.variable.view.config;

import org.junit.jupiter.api.Test;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.core.component.variable.view.help.VariablePluginHelp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class VariablePluginHelpConfigTest {

    private final VariablePluginHelpConfig variablePluginHelpConfig = new VariablePluginHelpConfig();

    @Test
    void whenTitleThenReturnsVariablePluginHelp() {
        assertEquals("Variable plugin help", variablePluginHelpConfig.title());
    }

    @Test
    void whenIconThenReturnsQuestionIconUrl() {
        assertEquals(PacosIcon.QUESTION.getUrl(), variablePluginHelpConfig.icon());
    }

    @Test
    void whenActivatorClassThenReturnsVariablePluginHelpClass() {
        assertEquals(VariablePluginHelp.class, variablePluginHelpConfig.activatorClass());
    }

    @Test
    void whenIsApplicationThenReturnsFalse() {
        assertFalse(variablePluginHelpConfig.isApplication());
    }

    @Test
    void whenIsAllowMultipleInstanceThenReturnsFalse() {
        assertFalse(variablePluginHelpConfig.isAllowMultipleInstance());
    }
}
