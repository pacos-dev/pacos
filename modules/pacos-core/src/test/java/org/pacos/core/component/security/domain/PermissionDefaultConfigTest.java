package org.pacos.core.component.security.domain;

import org.junit.jupiter.api.Test;
import org.pacos.base.session.AccessDecision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PermissionDefaultConfigTest {

    @Test
    void whenCreateEmptyConstructorThenFieldsAreNull() {
        PermissionDefaultConfig config = new PermissionDefaultConfig();
        assertNull(config.getAction());
        assertNull(config.getDecision());
        assertNull(config.getKey());
    }

    @Test
    void whenCreateWithActionThenDecisionIsDenyAndActionSet() {
        Permissions action = new Permissions();
        PermissionDefaultConfig config = new PermissionDefaultConfig(action);
        assertEquals(action, config.getAction());
        assertEquals(AccessDecision.DENY, config.getDecision());
    }

    @Test
    void whenSettersCalledThenValuesUpdated() {
        Permissions action = new Permissions();
        PermissionDefaultConfig config = new PermissionDefaultConfig();
        config.setAction(action);
        config.setDecision(AccessDecision.DENY);
        config.setKey("testKey");

        assertEquals("testKey", config.getKey());
        assertEquals(action, config.getAction());
        assertEquals(AccessDecision.DENY, config.getDecision());
    }
}
