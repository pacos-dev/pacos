package org.pacos.core.component.plugin.view.config;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.plugin.view.tab.PluginManagementTabLayout;
import org.pacos.core.component.security.SystemPermissions;
import org.springframework.stereotype.Component;

@Component
public class PluginManagementConfig implements SettingTab {

    private final PluginManager pluginManager;
    private final PluginProxy pluginProxy;

    public PluginManagementConfig(PluginManager pluginManager, PluginProxy pluginProxy) {
        this.pluginManager = pluginManager;
        this.pluginProxy = pluginProxy;
    }

    @Override
    public String getTitle() {
        return "Plugin management";
    }

    @Override
    public SettingPageLayout generateContent() {
        return new PluginManagementTabLayout(pluginManager, pluginProxy);
    }

    @Override
    public int getOrder() {
        return 71;
    }

    public boolean shouldBeDisplayed(UserSession userSession) {
        return UserSession.getCurrent().hasPermission(SystemPermissions.PLUGIN_MANAGEMENT_TAB_VISIBLE);
    }

    @Override
    public String getSearchIndex() {
        return PluginManagementTabLayout.getSearchIndex();
    }
}
