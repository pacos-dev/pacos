package org.pacos.core.component.settings.view.config;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.settings.view.tab.SystemBackgroundLayout;
import org.springframework.stereotype.Component;

@Component
public class SystemBackgroundConfig implements SettingTab {

    private final RegistryProxy registryProxy;

    public SystemBackgroundConfig(RegistryProxy registryProxy) {
        this.registryProxy = registryProxy;
    }

    @Override
    public String getTitle() {
        return SettingTabName.SYSTEM_BACKGROUND.getName();
    }

    @Override
    public SettingPageLayout generateContent() {
        return new SystemBackgroundLayout(registryProxy);
    }

    @Override
    public int getOrder() {
        return 1;
    }

    public boolean shouldBeDisplayed(UserSession userSession) {
        return userSession.hasPermission(SystemPermissions.SYSTEM_BACKGROUND_CONFIGURATION);
    }

    @Override
    public String[] getGroup() {
        return new String[] {SettingTabName.SYSTEM.getName()};
    }

    @Override
    public String getSearchIndex() {
        return SystemBackgroundLayout.getSearchIndex();
    }
}
