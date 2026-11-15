package org.pacos.core.component.installer.service;

import org.pacos.base.session.UserDTO;
import org.pacos.core.component.installer.settings.InstallerSettings;
import org.pacos.core.component.security.domain.Role;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.user.service.UserForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates user configuration.
 * 'system' user is used by pacos to manage guest account
 */
@Component
public class UserConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(UserConfiguration.class);
    private final UserProxyService userProxyService;

    @Autowired
    public UserConfiguration(UserProxyService userProxyService) {
        this.userProxyService = userProxyService;
    }

    /**
     * Creates or resolves the initial account and grants it the root role.
     *
     * @param installerSettings installation mode and initial account data
     * @return the initial account, or {@code null} when it cannot be resolved
     */
    @Transactional("coreTransactionManager")
    public UserDTO configure(InstallerSettings installerSettings) {
        String admin = "admin";
        String login = installerSettings.getInstallationMode().isSingle() ? admin : installerSettings.getAccountData().getLogin();
        if (userProxyService.checkIfLoginNoExists("guest")) {
            LOG.debug("Creating system user account");
            UserDTO guest = userProxyService.createAccount(new UserForm("guest",
                    "sadxcvsdfewr^%$&$@", "sadxcvsdfewr^%$&$@"));
            userProxyService.getUserService().addRole(Role.GUEST_ROLE, guest.getId());
        }
        UserDTO user;
        if (userProxyService.checkIfLoginNoExists(login)) {
            LOG.debug("Creating user defined account");
            if (installerSettings.getInstallationMode().isSingle()) {
                user = userProxyService.createAccount(new UserForm(admin, admin, admin));
            } else {
                user = userProxyService.createAccount(installerSettings.getAccountData());
            }
        } else {
            user = userProxyService.loadUserByLogin(login).orElse(null);
        }
        if (user != null) {
            userProxyService.getUserService().addRole(Role.ROOT_ROLE, user.getId());
        }
        return user;
    }
}
