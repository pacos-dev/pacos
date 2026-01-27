package org.pacos.core.component.settings.view.config.access;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.SystemPermissions;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class RolesManagementTabConfig implements SettingTab {

    private final ApplicationContext context;

    public RolesManagementTabConfig(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public String getTitle() {
        return SettingTabName.ROLES.getName();
    }

    @Override
    public SettingPageLayout generateContent() {
        return context.getBean(RolesManagementTabView.class);
    }

    @Override
    public String[] getGroup() {
        return new String[] { SettingTabName.ACCESS_MANAGEMENT.getName() };
    }

    @Override
    public int getOrder() {
        return 150;
    }

    @Override
    public boolean shouldBeDisplayed(UserSession userSession) {
        return userSession.hasPermission(SystemPermissions.USER_PERMISSIONS_TAB_VISIBLE) ||
                userSession.hasPermission(SystemPermissions.DEFAULT_PERMISSIONS_TAB_VISIBLE);
    }

}