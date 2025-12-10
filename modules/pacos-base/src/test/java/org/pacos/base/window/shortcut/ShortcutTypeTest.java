package org.pacos.base.window.shortcut;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShortcutTypeTest {

    @Test
    void whenGetShortcutCalled_thenReturnsExpectedShortcut() {
        assertEquals(new Shortcut(Key.KEY_S, KeyModifier.CONTROL), ShortcutType.SAVE.getShortcut());
        assertEquals(new Shortcut(Key.KEY_C, KeyModifier.CONTROL), ShortcutType.COPY.getShortcut());
        assertEquals(new Shortcut(Key.KEY_V, KeyModifier.CONTROL), ShortcutType.PASTE.getShortcut());
        assertEquals(new Shortcut(Key.KEY_X, KeyModifier.CONTROL), ShortcutType.CUT.getShortcut());
        assertEquals(new Shortcut(Key.DELETE), ShortcutType.DELETE.getShortcut());
        assertEquals(new Shortcut(Key.KEY_F, KeyModifier.CONTROL), ShortcutType.SEARCH.getShortcut());
    }

    @Test
    void whenIsSaveCalled_thenReturnsTrueForSaveShortcutType() {
        assertTrue(ShortcutType.SAVE.isSave());
        assertFalse(ShortcutType.COPY.isSave());
        assertFalse(ShortcutType.PASTE.isSave());
        assertFalse(ShortcutType.CUT.isSave());
        assertFalse(ShortcutType.DELETE.isSave());
        assertFalse(ShortcutType.SEARCH.isSave());
    }

    @Test
    void whenIsDeleteCalled_thenReturnsTrueForDeleteShortcutType() {
        assertTrue(ShortcutType.DELETE.isDelete());
        assertFalse(ShortcutType.SAVE.isDelete());
        assertFalse(ShortcutType.COPY.isDelete());
        assertFalse(ShortcutType.PASTE.isDelete());
        assertFalse(ShortcutType.CUT.isDelete());
        assertFalse(ShortcutType.SEARCH.isDelete());
    }
}