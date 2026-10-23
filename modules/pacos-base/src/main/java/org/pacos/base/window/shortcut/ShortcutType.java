package org.pacos.base.window.shortcut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private final List<ShortcutSequence> sequences;

    ShortcutType(Shortcut shortcut, ShortcutSequence... additionalSequences) {
        this.shortcut = shortcut;
        List<ShortcutSequence> definitions = new ArrayList<>();
        definitions.add(new ShortcutSequence(shortcut));
        definitions.addAll(Arrays.asList(additionalSequences));
        this.sequences = ShortcutSequence.withMacOsAlternatives(definitions);
    }

    public Shortcut getShortcut() {
        return shortcut;
    }

    /**
     * Returns all accepted sequences for this definition, including the macOS modifier alternative
     * where the shortcut uses CONTROL.
     */
    public List<ShortcutSequence> getSequences() {
        return sequences;
    }

    public boolean isSave() {
        return this == SAVE;
    }

    public boolean isDelete() {
        return this == DELETE;
    }
}
