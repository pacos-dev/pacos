package org.pacos.core.component.settings.view.config;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.SystemPermissions;
import org.springframework.stereotype.Component;

@Component
public class PermissionConfig implements SettingTab {
    @Override
    public String getTitle() {
        return SettingTabName.PERMISSIONS.getName();
    }

    @Override
    public SettingPageLayout generateContent() {
        return new PermissionConfigView();
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
