package org.pacos.core.component.settings.view.config;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.settings.view.tab.SystemAccessTabLayout;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SystemAccessConfig implements SettingTab {

    private final ApplicationContext context;

    public SystemAccessConfig(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public String getTitle() {
        return SettingTabName.ACCESS_MANAGEMENT.getName();
    }

    @Override
    public SettingPageLayout generateContent() {
        return context.getBean(SystemAccessTabLayout.class);
    }

    @Override
    public int getOrder() {
        return 200;
    }

    public boolean shouldBeDisplayed(UserSession userSession) {
        return userSession.hasPermission(SystemPermissions.SYSTEM_ACCESS_TAB_VISIBLE);
    }

    @Override
    public String getSearchIndex() {
        return SystemAccessTabLayout.getSearchIndex();
    }
}
