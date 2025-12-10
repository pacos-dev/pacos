package org.pacos.core.component.token.view.config;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.token.service.ApiTokenService;
import org.pacos.core.component.token.view.ApiConfigTabLayout;
import org.springframework.stereotype.Component;

@Component
public class ApiTokenTabConfig implements SettingTab {

    private final ApiTokenService tokenService;

    public ApiTokenTabConfig(ApiTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public String getTitle() {
        return "API access";
    }

    @Override
    public SettingPageLayout generateContent() {
        return new ApiConfigTabLayout(tokenService);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean shouldBeDisplayed(UserSession userSession) {
        return userSession.hasPermission(SystemPermissions.API_TAB_VISIBLE);
    }
}
