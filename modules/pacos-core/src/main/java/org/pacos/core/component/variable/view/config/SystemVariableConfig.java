package org.pacos.core.component.variable.view.config;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.variable.proxy.SystemVariableProxy;
import org.pacos.core.component.variable.view.global.SystemVariableSettings;
import org.springframework.stereotype.Component;

@Component
public class SystemVariableConfig implements SettingTab {

    private final SystemVariableProxy systemVariableProxy;

    public SystemVariableConfig(SystemVariableProxy systemVariableProxy) {
        this.systemVariableProxy = systemVariableProxy;
    }

    @Override
    public String getTitle() {
        return "System variables";
    }

    @Override
    public SettingPageLayout generateContent() {
        return new SystemVariableSettings(systemVariableProxy);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean shouldBeDisplayed(UserSession userSession) {
        return true;
    }
}
