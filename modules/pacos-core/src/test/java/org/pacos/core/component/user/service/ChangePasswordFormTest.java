package org.pacos.core.component.user.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChangePasswordFormTest {

    @Test
    void whenSetAndGetCurrentPasswordThenCorrect() {
        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("oldPassword");
        assertEquals("oldPassword", form.getCurrentPassword());
    }

    @Test
    void whenSetAndGetNewPasswordThenCorrect() {
        ChangePasswordForm form = new ChangePasswordForm();
        form.setNewPassword("newPassword123");
        assertEquals("newPassword123", form.getNewPassword());
    }

    @Test
    void whenSetAndGetRepeatPasswordThenCorrect() {
        ChangePasswordForm form = new ChangePasswordForm();
        form.setRepeatPassword("newPassword123");
        assertEquals("newPassword123", form.getRepeatPassword());
    }

    @Test
    void whenCreateChangePasswordFormThenAllFieldsAreNull() {
        ChangePasswordForm form = new ChangePasswordForm();
        assertNull(form.getCurrentPassword());
        assertNull(form.getNewPassword());
        assertNull(form.getRepeatPassword());
    }
}
