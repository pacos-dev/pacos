package org.pacos.core.component.user.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserTest {

    @Test
    void whenSetAndGetIdThenCorrect() {
        User user = new User();
        user.setId(1);
        assertEquals(1, user.getId());
    }

    @Test
    void whenSetAndGetLoginThenCorrect() {
        User user = new User();
        user.setLogin("testLogin");
        assertEquals("testLogin", user.getLogin());
    }

    @Test
    void whenSetAndGetPasswordHashThenCorrect() {
        User user = new User();
        user.setPasswordHash("hashedPassword");
        assertEquals("hashedPassword", user.getPasswordHash());
    }

    @Test
    void whenSetAndGetTokenThenCorrect() {
        User user = new User();
        user.setToken("testToken");
        assertEquals("testToken", user.getToken());
    }

    @Test
    void whenSetAndGetVariableCollectionIdThenCorrect() {
        User user = new User();
        user.setVariableCollectionId(42);
        assertEquals(42, user.getVariableCollectionId());
    }

    @Test
    void whenCreateUserThenAllFieldsAreNull() {
        User user = new User();
        assertNull(user.getId());
        assertNull(user.getLogin());
        assertNull(user.getPasswordHash());
        assertNull(user.getToken());
        assertNull(user.getVariableCollectionId());
    }
}
