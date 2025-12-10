package org.pacos.core.component.dock.view.settings;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DockSettingsConfig implements SettingTab {

    private final RegistryProxy registryProxy;

    @Autowired
    public DockSettingsConfig(RegistryProxy registryProxy) {
        this.registryProxy = registryProxy;
    }

    @Override
    public String getTitle() {
        return "Dock";
    }

    @Override
    public SettingPageLayout generateContent() {
        return new DockSettings(registryProxy);
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public boolean shouldBeDisplayed(UserSession userSession) {
        return !registryProxy.isSingleMode();
    }
}
