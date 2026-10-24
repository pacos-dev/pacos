package org.pacos.core.component.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.user.domain.User;
import org.pacos.core.component.user.repository.UserRepository;
import org.pacos.core.system.view.login.LoginForm;

class UserCredentialsServiceTest {

    private static final String LEGACY_HASH = "100:000102030405060708090a0b0c0d0e0f:"
            + "e2993bf8f7a1b14ad287ab24685a3f361a598e7cd4af87268f782353473a2fe0e"
            + "53bd38f6b67362baf94d196ef0a3304accca248c37d1be420bd28254d7429b2";

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserCredentialsService service = new UserCredentialsService(userRepository);

    @Test
    void whenHashPasswordThenReturnCurrentHash() {
        String hash = service.hashPassword("password");

        assertTrue(ShaHashAlgorithm.validatePassword("password", hash));
        assertFalse(ShaHashAlgorithm.needsRehash(hash));
    }

    @Test
    void whenIsValidCredentialsWithCurrentHashThenReturnTrueWithoutSaving() {
        User user = storedUser(service.hashPassword("password"));
        String originalHash = user.getPasswordHash();

        assertTrue(service.isValidCredentials(new LoginForm("login", "password")));

        assertEquals(originalHash, user.getPasswordHash());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenIsValidCredentialsWithMissingUserThenReturnFalse() {
        assertFalse(service.isValidCredentials(new LoginForm("missing", "password")));

        verify(userRepository).findByLogin("missing");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void whenIsValidCredentialsWithWrongPasswordThenLeaveHashUnchanged() {
        User user = storedUser(LEGACY_HASH);

        assertFalse(service.isValidCredentials(new LoginForm("login", "wrong")));

        assertEquals(LEGACY_HASH, user.getPasswordHash());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenIsValidCredentialsWithLegacyHashThenUpgradeHash() {
        User user = storedUser(LEGACY_HASH);

        assertTrue(service.isValidCredentials(new LoginForm("login", "test_password")));

        assertTrue(ShaHashAlgorithm.validatePassword("test_password", user.getPasswordHash()));
        assertFalse(ShaHashAlgorithm.needsRehash(user.getPasswordHash()));
        verify(userRepository).save(user);
    }

    @Test
    void whenIsValidCredentialsWithMalformedHashThenReturnFalseWithoutSaving() {
        User user = storedUser("malformed");

        assertFalse(service.isValidCredentials(new LoginForm("login", "password")));

        assertEquals("malformed", user.getPasswordHash());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenChangePasswordThenValidateCurrentPasswordFromChangeFormAndSaveOnce() {
        User user = storedUser(service.hashPassword("password"));
        LoginForm loginForm = new LoginForm("login", "not-the-current-password");
        ChangePasswordForm form = new ChangePasswordForm("password", "newPassword", "newPassword");

        assertTrue(service.changePassword(loginForm, form));

        assertTrue(ShaHashAlgorithm.validatePassword("newPassword", user.getPasswordHash()));
        assertFalse(ShaHashAlgorithm.validatePassword("password", user.getPasswordHash()));
        verify(userRepository).findByLogin("login");
        verify(userRepository).save(user);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void whenChangePasswordWithLegacyHashThenSaveNewHashOnlyOnce() {
        User user = storedUser(LEGACY_HASH);
        LoginForm loginForm = new LoginForm("login", "test_password");
        ChangePasswordForm form = new ChangePasswordForm("test_password", "newPassword", "newPassword");

        assertTrue(service.changePassword(loginForm, form));

        assertNotEquals(LEGACY_HASH, user.getPasswordHash());
        assertTrue(ShaHashAlgorithm.validatePassword("newPassword", user.getPasswordHash()));
        assertFalse(ShaHashAlgorithm.needsRehash(user.getPasswordHash()));
        verify(userRepository).findByLogin("login");
        verify(userRepository).save(user);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void whenChangePasswordWithWrongCurrentPasswordThenLeaveHashUnchanged() {
        User user = storedUser(service.hashPassword("password"));
        String originalHash = user.getPasswordHash();
        LoginForm loginForm = new LoginForm("login", "password");
        ChangePasswordForm form = new ChangePasswordForm("wrong", "newPassword", "newPassword");

        assertFalse(service.changePassword(loginForm, form));

        assertEquals(originalHash, user.getPasswordHash());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenChangePasswordWithMissingUserThenReturnFalseWithoutSaving() {
        LoginForm loginForm = new LoginForm("missing", "password");
        ChangePasswordForm form = new ChangePasswordForm("password", "newPassword", "newPassword");

        assertFalse(service.changePassword(loginForm, form));

        verify(userRepository).findByLogin("missing");
        verifyNoMoreInteractions(userRepository);
    }

    private User storedUser(String passwordHash) {
        User user = new User();
        user.setLogin("login");
        user.setPasswordHash(passwordHash);
        when(userRepository.findByLogin("login")).thenReturn(Optional.of(user));
        return user;
    }
}
