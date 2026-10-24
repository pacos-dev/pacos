package org.pacos.base.window.shortcut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;

class ShortcutSequenceTest {

    @Test
    void whenCreateSequenceThenPreserveStepOrder() {
        ShortcutSequence sequence = new ShortcutSequence(
                new Shortcut(Key.KEY_C, KeyModifier.CONTROL),
                new Shortcut(Key.KEY_S, KeyModifier.CONTROL));

        assertEquals(List.of(
                new Shortcut(Key.KEY_C, KeyModifier.CONTROL),
                new Shortcut(Key.KEY_S, KeyModifier.CONTROL)), sequence.steps());
    }

    @Test
    void whenWithMacOsAlternativesThenCreateMetaSequence() {
        ShortcutSequence sequence = new ShortcutSequence(
                new Shortcut(Key.KEY_C, KeyModifier.CONTROL),
                new Shortcut(Key.KEY_S, KeyModifier.CONTROL));

        assertEquals(List.of(
                sequence,
                new ShortcutSequence(
                        new Shortcut(Key.KEY_C, KeyModifier.META),
                        new Shortcut(Key.KEY_S, KeyModifier.META))),
                ShortcutSequence.withMacOsAlternatives(List.of(sequence)));
    }

    @Test
    void whenSelectPlatformThenUseControlOnNonMacOs() {
        ShortcutSequence control = new ShortcutSequence(new Shortcut(Key.KEY_C, KeyModifier.CONTROL));
        ShortcutSequence meta = new ShortcutSequence(new Shortcut(Key.KEY_C, KeyModifier.META));

        assertEquals(List.of(control), ShortcutSequence.forPlatform(List.of(control, meta), false));
    }

    @Test
    void whenSelectPlatformThenUseMetaOnMacOs() {
        ShortcutSequence control = new ShortcutSequence(new Shortcut(Key.KEY_C, KeyModifier.CONTROL));
        ShortcutSequence meta = new ShortcutSequence(new Shortcut(Key.KEY_C, KeyModifier.META));

        assertEquals(List.of(meta), ShortcutSequence.forPlatform(List.of(control, meta), true));
    }

    @Test
    void whenCreateSequenceWithoutStepsThenThrowException() {
        List<Shortcut> shortcuts = List.of();
        assertThrows(IllegalArgumentException.class, () -> new ShortcutSequence(shortcuts));
    }

    @Test
    void whenCreateSequenceThenStepsAreImmutable() {
        ShortcutSequence sequence = new ShortcutSequence(new Shortcut(Key.KEY_C, KeyModifier.CONTROL));

        List<Shortcut> shortcuts = sequence.steps();
        Shortcut controlS = new Shortcut(Key.KEY_C, KeyModifier.CONTROL);
        assertThrows(UnsupportedOperationException.class,
                () -> shortcuts.add(controlS));
    }
}
