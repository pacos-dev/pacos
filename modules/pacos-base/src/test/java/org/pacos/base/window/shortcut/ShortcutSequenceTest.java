package org.pacos.base.window.shortcut;

import java.util.List;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void whenCreateSequenceWithoutStepsThenThrowException() {
        assertThrows(IllegalArgumentException.class, () -> new ShortcutSequence(List.of()));
    }

    @Test
    void whenCreateSequenceThenStepsAreImmutable() {
        ShortcutSequence sequence = new ShortcutSequence(new Shortcut(Key.KEY_C, KeyModifier.CONTROL));

        assertThrows(UnsupportedOperationException.class,
                () -> sequence.steps().add(new Shortcut(Key.KEY_S, KeyModifier.CONTROL)));
    }
}
