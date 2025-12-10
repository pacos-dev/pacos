package org.pacos.core.component.settings.view.config;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.settings.view.SystemAccessTabLayout;
import org.springframework.stereotype.Component;

@Component
public class SystemAccessConfig implements SettingTab {

    private final RegistryProxy registryProxy;

    public SystemAccessConfig(RegistryProxy registryProxy) {
        this.registryProxy = registryProxy;
    }

    @Override
    public String getTitle() {
        return "System access";
    }

    @Override
    public SettingPageLayout generateContent() {
        return new SystemAccessTabLayout(registryProxy);
    }

    @Override
    public int getOrder() {
        return 100;
    }

    public boolean shouldBeDisplayed(UserSession userSession) {
        return userSession.hasPermission(SystemPermissions.SYSTEM_ACCESS_TAB_VISIBLE);
    }
}
