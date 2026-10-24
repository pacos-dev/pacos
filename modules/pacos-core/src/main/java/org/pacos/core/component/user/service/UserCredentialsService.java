package org.pacos.core.component.user.service;

import java.util.Optional;

import org.pacos.core.component.user.domain.User;
import org.pacos.core.component.user.repository.UserRepository;
import org.pacos.core.system.view.login.LoginForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserCredentialsService {

    private final UserRepository userRepository;

    public String hashPassword(String password) {
        return ShaHashAlgorithm.hashPassword(password);
    }

    @Transactional("coreTransactionManager")
    public boolean isValidCredentials(LoginForm loginForm) {
        Optional<User> user = userRepository.findByLogin(loginForm.getLogin());
        if (user.isEmpty()) {
            return false;
        }

        User account = user.get();
        if (!ShaHashAlgorithm.validatePassword(loginForm.getPassword(), account.getPasswordHash())) {
            return false;
        }

        if (ShaHashAlgorithm.needsRehash(account.getPasswordHash())) {
            account.setPasswordHash(ShaHashAlgorithm.hashPassword(loginForm.getPassword()));
            userRepository.save(account);
        }
        return true;
    }

    @Transactional("coreTransactionManager")
    public boolean changePassword(LoginForm loginForm, ChangePasswordForm changePasswordForm) {
        Optional<User> user = userRepository.findByLogin(loginForm.getLogin());
        if (user.isEmpty()) {
            return false;
        }

        User account = user.get();
        if (!ShaHashAlgorithm.validatePassword(changePasswordForm.getCurrentPassword(), account.getPasswordHash())) {
            return false;
        }

        account.setPasswordHash(ShaHashAlgorithm.hashPassword(changePasswordForm.getNewPassword()));
        userRepository.save(account);
        return true;
    }
}
