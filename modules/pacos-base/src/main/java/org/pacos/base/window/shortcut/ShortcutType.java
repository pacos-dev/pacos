package org.pacos.base.window.shortcut;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;

/**
 * Stores key combination configurations for popular actions
 */
public enum ShortcutType {
    SAVE(new Shortcut(Key.KEY_S, KeyModifier.CONTROL)),
    COPY(new Shortcut(Key.KEY_C, KeyModifier.CONTROL)),
    PASTE(new Shortcut(Key.KEY_V, KeyModifier.CONTROL)),
    CUT(new Shortcut(Key.KEY_X, KeyModifier.CONTROL)),
    DELETE(new Shortcut(Key.DELETE)),
    SEARCH(new Shortcut(Key.KEY_F, KeyModifier.CONTROL)),
    MOVE_ON(new Shortcut(Key.ARROW_RIGHT, KeyModifier.ALT)),
    MOVE_BACK(new Shortcut(Key.ARROW_LEFT, KeyModifier.ALT)),
    BACKSPACE(new Shortcut(Key.BACKSPACE));

    private final Shortcut shortcut;

    ShortcutType(Shortcut shortcut) {
        this.shortcut = shortcut;
    }

    public Shortcut getShortcut() {
        return shortcut;
    }

    public boolean isSave() {
        return this == SAVE;
    }

    public boolean isDelete() {
        return this == DELETE;
    }
}
