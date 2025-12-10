package org.pacos.base.window.shortcut;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ShortcutTest {

    @Test
    void whenSameInstanceThenEqualsReturnsTrue() {
        Shortcut shortcut1 = new Shortcut(Key.KEY_A, KeyModifier.CONTROL, KeyModifier.SHIFT);
        assertEquals(shortcut1, shortcut1);
    }

    @Test
    void whenDifferentClassThenEqualsReturnsFalse() {
        Shortcut shortcut1 = new Shortcut(Key.KEY_A, KeyModifier.CONTROL, KeyModifier.SHIFT);
        assertNotEquals("Not a Shortcut", shortcut1);
    }

    @Test
    void whenSameFieldsThenEqualsReturnsTrue() {
        Shortcut shortcut1 = new Shortcut(Key.KEY_A, KeyModifier.CONTROL, KeyModifier.SHIFT);
        Shortcut shortcut2 = new Shortcut(Key.KEY_A, KeyModifier.CONTROL, KeyModifier.SHIFT);
        assertEquals(shortcut1, shortcut2);
    }

    @Test
    void whenDifferentKeyThenEqualsReturnsFalse() {
        Shortcut shortcut1 = new Shortcut(Key.KEY_A, KeyModifier.CONTROL, KeyModifier.SHIFT);
        Shortcut shortcut2 = new Shortcut(Key.KEY_B, KeyModifier.CONTROL, KeyModifier.SHIFT);
        assertNotEquals(shortcut1, shortcut2);
    }

    @Test
    void whenDifferentModifiersThenEqualsReturnsFalse() {
        Shortcut shortcut1 = new Shortcut(Key.KEY_A, KeyModifier.CONTROL, KeyModifier.SHIFT);
        Shortcut shortcut2 = new Shortcut(Key.KEY_A, KeyModifier.ALT);
        assertNotEquals(shortcut1, shortcut2);
    }

    @Test
    void whenSameFieldsThenHashCodeIsSame() {
        Shortcut shortcut1 = new Shortcut(Key.KEY_A, KeyModifier.CONTROL, KeyModifier.SHIFT);
        Shortcut shortcut2 = new Shortcut(Key.KEY_A, KeyModifier.CONTROL, KeyModifier.SHIFT);
        assertEquals(shortcut1.hashCode(), shortcut2.hashCode());
    }

    @Test
    void whenDifferentFieldsThenHashCodeIsDifferent() {
        Shortcut shortcut1 = new Shortcut(Key.KEY_A, KeyModifier.CONTROL, KeyModifier.SHIFT);
        Shortcut shortcut2 = new Shortcut(Key.KEY_B, KeyModifier.CONTROL, KeyModifier.SHIFT);
        assertNotEquals(shortcut1.hashCode(), shortcut2.hashCode());
    }

    @Test
    void whenToStringCalledThenReturnExpectedString() {
        Shortcut shortcut = new Shortcut(Key.KEY_A, KeyModifier.CONTROL, KeyModifier.SHIFT);
        String expectedString = "Shortcut{key=KeyA, keyModifiers=[CONTROL, SHIFT]}";
        assertEquals(expectedString, shortcut.toString());
    }
}