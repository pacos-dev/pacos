package org.pacos.base.window.shortcut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.vaadin.flow.component.KeyModifier;

/**
 * An ordered sequence of keyboard shortcuts. Each {@link Shortcut} is one key press with its modifiers.
 */
public record ShortcutSequence(List<Shortcut> steps) {

    public static final long DEFAULT_SEQUENCE_TIMEOUT_MILLIS = 1500L;

    public ShortcutSequence {
        Objects.requireNonNull(steps, "steps");
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("A shortcut sequence must contain at least one step");
        }
        steps = List.copyOf(steps);
    }

    public ShortcutSequence(Shortcut firstStep, Shortcut... remainingSteps) {
        this(combine(firstStep, remainingSteps));
    }

    public static ShortcutSequence of(Shortcut... steps) {
        Objects.requireNonNull(steps, "steps");
        return new ShortcutSequence(List.of(steps));
    }

    /**
     * Adds a macOS-friendly alternative by replacing CONTROL with META across a sequence.
     * The original sequence is retained, so a definition works on both Windows/Linux and macOS
     * without waiting for asynchronous client OS detection.
     */
    public static List<ShortcutSequence> withMacOsAlternatives(
            Collection<ShortcutSequence> definitions) {
        Objects.requireNonNull(definitions, "definitions");
        if (definitions.isEmpty()) {
            throw new IllegalArgumentException("At least one shortcut sequence is required");
        }

        Set<ShortcutSequence> alternatives = new LinkedHashSet<>();
        for (ShortcutSequence definition : definitions) {
            ShortcutSequence sequence = Objects.requireNonNull(definition, "definition");
            alternatives.add(sequence);
            ShortcutSequence macOsAlternative = sequence.withMetaModifiers();
            if (macOsAlternative != null) {
                alternatives.add(macOsAlternative);
            }
        }
        return List.copyOf(alternatives);
    }

    private static List<Shortcut> combine(Shortcut firstStep, Shortcut[] remainingSteps) {
        Objects.requireNonNull(firstStep, "firstStep");
        Objects.requireNonNull(remainingSteps, "remainingSteps");
        List<Shortcut> steps = new ArrayList<>(remainingSteps.length + 1);
        steps.add(firstStep);
        steps.addAll(List.of(remainingSteps));
        return steps;
    }

    private ShortcutSequence withMetaModifiers() {
        boolean hasControlModifier = steps.stream()
                .anyMatch(step -> containsModifier(step, KeyModifier.CONTROL));
        boolean hasCombinedControlAndMeta = steps.stream()
                .anyMatch(step -> containsModifier(step, KeyModifier.CONTROL)
                        && containsModifier(step, KeyModifier.META));
        if (!hasControlModifier || hasCombinedControlAndMeta) {
            return null;
        }

        List<Shortcut> metaSteps = steps.stream()
                .map(step -> {
                    Set<KeyModifier> modifiers = new LinkedHashSet<>();
                    for (KeyModifier modifier : step.keyModifiers()) {
                        modifiers.add(modifier == KeyModifier.CONTROL ? KeyModifier.META : modifier);
                    }
                    return new Shortcut(step.key(), modifiers.toArray(KeyModifier[]::new));
                })
                .toList();
        ShortcutSequence alternative = new ShortcutSequence(metaSteps);
        return alternative.equals(this) ? null : alternative;
    }

    private static boolean containsModifier(Shortcut shortcut, KeyModifier modifier) {
        for (KeyModifier candidate : shortcut.keyModifiers()) {
            if (candidate == modifier) {
                return true;
            }
        }
        return false;
    }
}
