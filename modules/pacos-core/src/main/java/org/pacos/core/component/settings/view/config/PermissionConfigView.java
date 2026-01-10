package org.pacos.core.component.settings.view.config;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.utils.component.InfoBox;
import org.pacos.base.window.shortcut.ShortcutType;

public class PermissionConfigView extends SettingPageLayout {

    public PermissionConfigView() {
        add(new InfoBox("""
                Define global authorization policies for the system. Configure baseline access rights automatically
                assigned to newly created accounts and guest sessions. This module also allows for granular permission
                management for individual user accounts.
                """));
    }

    @Override
    public void onShortCutDetected(ShortcutType shortcutType) {
        //no detection
    }
}