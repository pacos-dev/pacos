package org.pacos.core.component.settings.view.config;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.settings.view.tab.SystemUpdateTabLayout;
import org.pacos.core.system.service.AutoUpdateService;
import org.springframework.stereotype.Component;

@Component
public class SystemUpdateConfig implements SettingTab {

    private final RegistryProxy registryProxy;
    private final AutoUpdateService autoUpdateService;

    public SystemUpdateConfig(RegistryProxy registryProxy, AutoUpdateService autoUpdateService) {
        this.registryProxy = registryProxy;
        this.autoUpdateService = autoUpdateService;
    }

    @Override
    public String getTitle() {
        return SettingTabName.SYSTEM_UPDATE.getName();
    }

    @Override
    public SettingPageLayout generateContent() {
        return new SystemUpdateTabLayout(registryProxy, autoUpdateService);
    }

    @Override
    public int getOrder() {
        return 101;
    }

    public boolean shouldBeDisplayed(UserSession userSession) {
        return true;
    }

    @Override
    public String getSearchIndex() {
        return SystemUpdateTabLayout.getSearchIndex();
    }

    @Override
    public String[] getGroup() {
        return new String[]{SettingTabName.SYSTEM.getName()};
    }

}
