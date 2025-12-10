package org.pacos.core.system.manager;

import com.vaadin.flow.component.ShortcutEventListener;
import org.pacos.base.window.shortcut.Shortcut;

/**
 * connects keyboard shortcut to listener
 */
public record ShortcutListener(Shortcut shortcut, ShortcutEventListener listener) {
}
