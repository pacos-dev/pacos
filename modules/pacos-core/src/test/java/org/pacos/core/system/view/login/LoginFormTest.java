package org.pacos.core.system.view.login;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginFormTest {

    @Test
    void whenUsingDefaultConstructorThenFieldsShouldBeInitializedCorrectly() {
        LoginForm loginForm = new LoginForm();
        assertNull(loginForm.getLogin());
        assertNull(loginForm.getPassword());
        assertTrue(loginForm.getRememberMe());
    }

    @Test
    void whenUsingParameterizedConstructorThenFieldsShouldBeSetCorrectly() {
        LoginForm loginForm = new LoginForm("testLogin", "testPassword");
        assertEquals("testLogin", loginForm.getLogin());
        assertEquals("testPassword", loginForm.getPassword());
        assertTrue(loginForm.getRememberMe());
    }

    @Test
    void whenSetLoginThenFieldShouldBeUpdated() {
        LoginForm loginForm = new LoginForm();
        loginForm.setLogin("updatedLogin");
        assertEquals("updatedLogin", loginForm.getLogin());
    }

    @Test
    void whenSetPasswordThenFieldShouldBeUpdated() {
        LoginForm loginForm = new LoginForm();
        loginForm.setPassword("updatedPassword");
        assertEquals("updatedPassword", loginForm.getPassword());
    }

    @Test
    void whenSetRememberMeThenFieldShouldBeUpdated() {
        LoginForm loginForm = new LoginForm();
        loginForm.setRememberMe(false);
        assertFalse(loginForm.getRememberMe());
    }
}
