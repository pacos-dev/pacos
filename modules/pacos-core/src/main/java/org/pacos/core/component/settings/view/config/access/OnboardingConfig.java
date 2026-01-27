package org.pacos.core.component.settings.view.config.access;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.SystemPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class OnboardingConfig implements SettingTab {

    private final ApplicationContext context;

    @Autowired
    public OnboardingConfig(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public String getTitle() {
        return SettingTabName.ONBOARDING.getName();
    }

    @Override
    public SettingPageLayout generateContent() {
        return context.getBean(OnboardingTabLayout.class);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean shouldBeDisplayed(UserSession userSession) {
        return UserSession.getCurrent().hasPermission(SystemPermissions.DEFAULT_PERMISSIONS_TAB_VISIBLE);
    }

    @Override
    public String[] getGroup() {
        return new String[] { SettingTabName.ACCESS_MANAGEMENT.getName() };
    }

    @Override
    public String getSearchIndex() {
        return OnboardingTabLayout.getSearchIndex();
    }
}
