package org.pacos.core.component.settings.view.config;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.settings.view.tab.DefaultPermissionsTabLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DefaultPermissionConfig implements SettingTab {

    private final ApplicationContext context;

    @Autowired
    public DefaultPermissionConfig(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public String getTitle() {
        return "Default permissions";
    }

    @Override
    public SettingPageLayout generateContent() {
        return context.getBean(DefaultPermissionsTabLayout.class);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean shouldBeDisplayed(UserSession userSession) {
        return UserSession.getCurrent().hasPermission(SystemPermissions.DEFAULT_PERMISSIONS_TAB_VISIBLE);
    }
}
