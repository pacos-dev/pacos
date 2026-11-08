package org.pacos.core.component.credential.view.config;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.core.component.credential.service.CredentialVaultService;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.credential.view.CredentialsTabLayout;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.security.service.RoleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CredentialsTabConfig implements SettingTab {

    private final CredentialVaultService credentialService;
    private final RoleService roleService;
    private final PluginProxy pluginProxy;
    private final long maxFileSizeBytes;

    public CredentialsTabConfig(
            CredentialVaultService credentialService,
            RoleService roleService,
            PluginProxy pluginProxy,
            @Value("${pacos.credential-vault.max-file-size-bytes:10485760}") long maxFileSizeBytes) {
        this.credentialService = credentialService;
        this.roleService = roleService;
        this.pluginProxy = pluginProxy;
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    @Override
    public String getTitle() {
        return "Credentials";
    }

    @Override
    public SettingPageLayout generateContent() {
        return new CredentialsTabLayout(
                credentialService, roleService, pluginProxy, UserSession.getCurrent(), maxFileSizeBytes);
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public boolean shouldBeDisplayed(UserSession userSession) {
        return userSession != null && userSession.hasPermission(SystemPermissions.CREDENTIALS_TAB_VISIBLE);
    }

    @Override
    public String getSearchIndex() {
        return CredentialsTabLayout.getSearchIndex();
    }
}
