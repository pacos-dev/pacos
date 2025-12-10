package org.pacos.core.component.security.domain;

import org.junit.jupiter.api.Test;
import org.pacos.base.session.AccessDecision;
import org.pacos.core.component.user.domain.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserPermissionTest {

    @Test
    void whenCreateEmptyObjectThenAllFieldsAreNull() {
        UserPermission userPermission = new UserPermission();
        assertNull(userPermission.getId());
        assertNull(userPermission.getUser());
        assertNull(userPermission.getAction());
        assertNull(userPermission.getDecision());
    }

    @Test
    void whenSettersCalledThenValuesAreUpdated() {
        User user = new User();
        Permissions action = new Permissions();

        UserPermission userPermission = new UserPermission();
        userPermission.setId(1);
        userPermission.setUser(user);
        userPermission.setAction(action);
        userPermission.setDecision(AccessDecision.ALLOW);

        assertEquals(1, userPermission.getId());
        assertEquals(user, userPermission.getUser());
        assertEquals(action, userPermission.getAction());
        assertEquals(AccessDecision.ALLOW, userPermission.getDecision());
    }
}
