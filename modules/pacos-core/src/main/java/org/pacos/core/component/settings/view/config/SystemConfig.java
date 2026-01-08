package org.pacos.core.component.settings.view.config;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.component.InfoBox;
import org.pacos.base.window.shortcut.ShortcutType;
import org.springframework.stereotype.Component;

@Component
public class SystemConfig implements SettingTab {

    @Override
    public String getTitle() {
        return SettingTabName.SYSTEM.getName();
    }

    @Override
    public SettingPageLayout generateContent() {
        SettingPageLayout layout = new SettingPageLayout() {
            @Override
            public void onShortCutDetected(ShortcutType shortcutType) {
                //not implemented
            }
        };
        layout.add(new InfoBox("Configure system behaviour"));
        return layout;
    }

    @Override
    public int getOrder() {
        return 101;
    }

    public boolean shouldBeDisplayed(UserSession userSession) {
        return true;
    }

    @Override
    public String getSearchIndex() {
        return "";
    }
}
