package org.pacos.core.component.variable.view.config;

import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.variable.view.PanelVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariableConfigTest {

    private final VariableConfig variableConfig = new VariableConfig();

    @Test
    void whenTitleThenReturnsVariables() {
        assertEquals("Variables", variableConfig.title());
    }

    @Test
    void whenIconThenReturnsVariableIconUrl() {
        assertEquals(PacosIcon.VARIABLE.getUrl(), variableConfig.icon());
    }

    @Test
    void whenActivatorClassThenReturnsPanelVariableClass() {
        assertEquals(PanelVariable.class, variableConfig.activatorClass());
    }

    @Test
    void whenIsApplicationThenReturnsTrue() {
        assertTrue(variableConfig.isApplication());
    }

    @Test
    void whenIsAllowMultipleInstanceThenReturnsFalse() {
        assertFalse(variableConfig.isAllowMultipleInstance());
    }

    @Test
    void whenIsAllowedForCurrentSessionAndGuestUserThenReturnsFalse() {
        VaadinMock.mockSystem(new UserDTO("Guest"));

        assertFalse(variableConfig.isAllowedForCurrentSession(UserSession.getCurrent()));
    }

    @Test
    void whenIsAllowedForCurrentSessionAndNonGuestUserThenReturnsTrue() {
        VaadinMock.mockSystem();

        assertTrue(variableConfig.isAllowedForCurrentSession(UserSession.getCurrent()));
    }
}
