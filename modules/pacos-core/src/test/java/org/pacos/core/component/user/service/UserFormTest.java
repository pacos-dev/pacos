package org.pacos.core.component.user.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserFormTest {

    @Test
    void whenSetAndGetLoginThenCorrect() {
        UserForm userForm = new UserForm();
        userForm.setLogin("testLogin");
        assertEquals("testLogin", userForm.getLogin());
    }

    @Test
    void whenSetAndGetPasswordThenCorrect() {
        UserForm userForm = new UserForm();
        userForm.setPassword("testPassword");
        assertEquals("testPassword", userForm.getPassword());
    }

    @Test
    void whenSetAndGetRepeatPasswordThenCorrect() {
        UserForm userForm = new UserForm();
        userForm.setRepeatPassword("testPassword");
        assertEquals("testPassword", userForm.getRepeatPassword());
    }

    @Test
    void whenCreateUserFormWithConstructorThenCorrect() {
        UserForm userForm = new UserForm("testLogin", "testPassword", "testPassword");
        assertEquals("testLogin", userForm.getLogin());
        assertEquals("testPassword", userForm.getPassword());
        assertEquals("testPassword", userForm.getRepeatPassword());
    }

    @Test
    void whenCreateUserFormWithoutConstructorThenFieldsAreNull() {
        UserForm userForm = new UserForm();
        assertNull(userForm.getLogin());
        assertNull(userForm.getPassword());
        assertNull(userForm.getRepeatPassword());
    }
}
