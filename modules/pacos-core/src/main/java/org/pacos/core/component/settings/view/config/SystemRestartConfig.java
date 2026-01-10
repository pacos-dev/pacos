package org.pacos.core.component.settings.view.config;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.settings.view.tab.SystemRestartLayout;
import org.springframework.stereotype.Component;

@Component
public class SystemRestartConfig implements SettingTab {

    @Override
    public String getTitle() {
        return SettingTabName.RESTART.getName();
    }

    @Override
    public SettingPageLayout generateContent() {
        return new SystemRestartLayout();
    }

    @Override
    public int getOrder() {
        return 1;
    }

    public boolean shouldBeDisplayed(UserSession userSession) {
        return userSession.hasPermission(SystemPermissions.SYSTEM_RESTART);
    }

    @Override
    public String[] getGroup() {
        return new String[]{SettingTabName.SYSTEM.getName()};
    }

    @Override
    public String getSearchIndex() {
        return SystemRestartLayout.getSearchIndex();
    }
}
