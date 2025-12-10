package org.pacos.base.component.setting;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.pacos.base.window.shortcut.ShortcutType;

/**
 * Functional interface for displayed setting page.
 */
public abstract class SettingPageLayout extends VerticalLayout {

    /**
     * Called when a registered shortcut is detected on the active settings page
     */
    public abstract void onShortCutDetected(ShortcutType shortcutType);

}
