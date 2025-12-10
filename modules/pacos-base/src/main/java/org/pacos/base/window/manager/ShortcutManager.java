package org.pacos.base.window.manager;

import java.io.Serializable;

import com.vaadin.flow.component.ShortcutEventListener;
import com.vaadin.flow.component.ShortcutRegistration;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.shortcut.Shortcut;

/**
 * It is responsible for handling keyboard shortcuts on the client side.
 * The listener is strongly associated with the window and can only be called if the window
 * is active at the time the event is registered.
 */
public interface ShortcutManager extends Serializable {
    /**
     * Register listener for given window
     */
    ShortcutRegistration registerShortcut(DesktopWindow desktopWindow, Shortcut shortcut, ShortcutEventListener shortcutEventListener);

    /**
     * Unregister listener for the given window
     */
    void unregisterAll(DesktopWindow desktopWindow);

    /**
     * Refresh all shortcuts listeners
     */
    void refreshShortcuts();
}
