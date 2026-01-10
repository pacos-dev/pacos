package org.pacos.core.component.settings.view.config;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.settings.view.tab.UserAccountsTabLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class UserAccountConfig implements SettingTab {

    private final ApplicationContext context;

    @Autowired
    public UserAccountConfig(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public String getTitle() {
        return SettingTabName.USER_PERMISSION.getName();
    }

    @Override
    public SettingPageLayout generateContent() {
        return context.getBean(UserAccountsTabLayout.class);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean shouldBeDisplayed(UserSession userSession) {
        return userSession.hasPermission(SystemPermissions.USER_PERMISSIONS_TAB_VISIBLE);
    }

    @Override
    public String[] getGroup() {
        return new String[]{SettingTabName.PERMISSIONS.getName()};
    }

    @Override
    public String getSearchIndex() {
        return UserAccountsTabLayout.getSearchIndex();
    }
}
