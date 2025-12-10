package org.pacos.core.component.user.service;

import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKeyFactory;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class ShaHashAlgorithmTest {

    @Test
    void whenHashPasswordThenReturnStringWithFixedLength() {
        //given
        String password = "test_password";
        //when
        String hashPassword = ShaHashAlgorithm.hashPassword(password);
        //then
        assertNotEquals(password, hashPassword);
        assertEquals(165, hashPassword.length());
    }

    @Test
    void whenHashPasswordTwiceThenHashIsDifferent() {
        //given
        String password = "test_password";
        //when
        String hashPassword = ShaHashAlgorithm.hashPassword(password);
        String hashPassword2 = ShaHashAlgorithm.hashPassword(password);
        //then
        assertNotEquals(hashPassword, hashPassword2);
    }

    @Test
    void whenValidatePasswordThenTrue() {
        //given
        String password = "test_password";
        //when
        String hashPassword = ShaHashAlgorithm.hashPassword(password);
        //then
        assertTrue(ShaHashAlgorithm.validatePassword(password, hashPassword));
    }

    @Test
    void whenValidatePasswordThenFalse() {
        //given
        String password = "test_password";
        String unMatchPassword = "test_password2";
        //when
        String hashPassword = ShaHashAlgorithm.hashPassword(password);
        //then
        assertFalse(ShaHashAlgorithm.validatePassword(unMatchPassword, hashPassword));
    }


    @Test
    void whenErrorWhileValidatingPasswordThenReturnFalse() {
        //given
        String password = "test_password";
        //when
        String hashPassword = ShaHashAlgorithm.hashPassword(password);
        try (MockedStatic<SecretKeyFactory> mock = mockStatic(SecretKeyFactory.class)) {
            mock.when(() -> SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"))
                    .thenThrow(NoSuchAlgorithmException.class);

            assertFalse(ShaHashAlgorithm.validatePassword(password, hashPassword));
        }
    }

}