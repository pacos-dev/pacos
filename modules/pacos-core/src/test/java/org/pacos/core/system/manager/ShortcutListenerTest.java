package org.pacos.core.system.manager;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutEventListener;
import org.junit.jupiter.api.Test;
import org.pacos.base.window.shortcut.Shortcut;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShortcutListenerTest {

    @Test
    void whenShortcutListenerIsCreatedThenFieldsShouldBeSetCorrectly() {
        Shortcut mockShortcut = new Shortcut(Key.KEY_S, KeyModifier.CONTROL);
        ShortcutEventListener mockListener = event -> {
        };

        ShortcutListener shortcutListener = new ShortcutListener(mockShortcut, mockListener);

        assertEquals(mockShortcut, shortcutListener.shortcut());
        assertEquals(mockListener, shortcutListener.listener());
    }
}