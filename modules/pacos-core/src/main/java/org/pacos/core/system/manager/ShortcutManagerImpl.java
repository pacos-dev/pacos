package org.pacos.core.system.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.vaadin.flow.component.ShortcutEvent;
import com.vaadin.flow.component.ShortcutEventListener;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.shared.Registration;
import org.pacos.base.event.UISystem;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.manager.ShortcutManager;
import org.pacos.base.window.shortcut.Shortcut;
import org.pacos.base.window.shortcut.ShortcutSequence;
import org.pacos.base.window.shortcut.ShortcutPlatformResolver;

public class ShortcutManagerImpl implements ShortcutManager, Serializable {

    private final Map<DesktopWindow, Set<SequenceShortcutListener>> shortcutListenerMap = new HashMap<>();
    private final Map<Shortcut, ShortcutRegistration> registeredShortcuts = new HashMap<>();

    @Override
    public ShortcutRegistration registerShortcut(DesktopWindow desktopWindow, Shortcut shortcut,
            ShortcutEventListener shortcutEventListener) {
        return registerShortcutSequences(desktopWindow, List.of(new ShortcutSequence(shortcut)),
                shortcutEventListener);
    }

    @Override
    public ShortcutRegistration registerShortcutSequences(DesktopWindow desktopWindow,
            List<ShortcutSequence> sequences, ShortcutEventListener shortcutEventListener) {
        SequenceShortcutListener listener = registerSequenceListener(desktopWindow,
                resolveSequencesForCurrentPlatform(sequences), shortcutEventListener);
        return registeredShortcuts.get(listener.sequences.get(0).steps().get(0));
    }

    @Override
    public Registration registerShortcutSequence(DesktopWindow desktopWindow, ShortcutSequence sequence,
            ShortcutEventListener shortcutEventListener) {
        SequenceShortcutListener listener = registerSequenceListener(desktopWindow,
                resolveSequencesForCurrentPlatform(List.of(sequence)), shortcutEventListener);
        return () -> unregisterSequenceListener(desktopWindow, listener);
    }

    @Override
    public void unregisterAll(DesktopWindow desktopWindow) {
        shortcutListenerMap.remove(desktopWindow);
        unregisterUnusedShortcuts();
    }

    @Override
    public void refreshShortcuts() {
        List<Shortcut> shortcuts = new ArrayList<>(registeredShortcuts.keySet());
        for (Shortcut shortcut : shortcuts) {
            ShortcutRegistration registration = registeredShortcuts.remove(shortcut);
            if (registration != null) {
                registration.remove();
            }
            registerShortcutInUi(shortcut);
        }
    }

    void shortCutDetected(ShortcutEvent event, Shortcut shortcut) {
        Optional<DesktopWindow> activeWindow = UISystem.getCurrent().getWindowManager().getWindowDisplayedOnFront();
        if (activeWindow.isEmpty()) {
            return;
        }

        Set<SequenceShortcutListener> listeners = shortcutListenerMap.get(activeWindow.get());
        if (listeners == null) {
            return;
        }

        SequenceShortcutListener matchingListener = null;
        for (SequenceShortcutListener listener : new ArrayList<>(listeners)) {
            if (listener.matches(shortcut) && matchingListener == null) {
                matchingListener = listener;
            }
        }
        if (matchingListener != null) {
            matchingListener.listener.onShortcut(event);
        }
    }

    private SequenceShortcutListener registerSequenceListener(DesktopWindow desktopWindow,
            List<ShortcutSequence> sequences, ShortcutEventListener shortcutEventListener) {
        List<ShortcutSequence> alternatives = List.copyOf(sequences);
        SequenceShortcutListener listener = new SequenceShortcutListener(alternatives, shortcutEventListener);
        shortcutListenerMap.computeIfAbsent(desktopWindow, window -> new HashSet<>()).add(listener);

        Set<Shortcut> keys = new LinkedHashSet<>();
        alternatives.forEach(sequence -> keys.addAll(sequence.steps()));
        keys.forEach(this::registerShortcutInUi);
        return listener;
    }

    private List<ShortcutSequence> resolveSequencesForCurrentPlatform(List<ShortcutSequence> sequences) {
        return ShortcutPlatformResolver.resolve(sequences);
    }

    private void registerShortcutInUi(Shortcut shortcut) {
        registeredShortcuts.computeIfAbsent(shortcut,
                key -> UI.getCurrent().addShortcutListener(event -> shortCutDetected(event, key),
                        key.key(), key.keyModifiers()));
    }

    private void unregisterSequenceListener(DesktopWindow desktopWindow, SequenceShortcutListener listener) {
        Set<SequenceShortcutListener> listeners = shortcutListenerMap.get(desktopWindow);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                shortcutListenerMap.remove(desktopWindow);
            }
        }
        unregisterUnusedShortcuts();
    }

    private void unregisterUnusedShortcuts() {
        Set<Shortcut> usedShortcuts = new HashSet<>();
        shortcutListenerMap.values().forEach(listeners ->
                listeners.forEach(listener ->
                        listener.sequences.forEach(sequence -> usedShortcuts.addAll(sequence.steps()))));

        List<Shortcut> unusedShortcuts = registeredShortcuts.keySet().stream()
                .filter(shortcut -> !usedShortcuts.contains(shortcut))
                .toList();
        for (Shortcut shortcut : unusedShortcuts) {
            ShortcutRegistration registration = registeredShortcuts.remove(shortcut);
            if (registration != null) {
                registration.remove();
            }
        }
    }

    private static final class SequenceShortcutListener {

        private final List<ShortcutSequence> sequences;
        private final ShortcutEventListener listener;
        private final Map<ShortcutSequence, SequenceProgress> progress = new HashMap<>();

        private SequenceShortcutListener(List<ShortcutSequence> sequences, ShortcutEventListener listener) {
            this.sequences = List.copyOf(sequences);
            this.listener = listener;
            this.sequences.forEach(sequence -> progress.put(sequence, new SequenceProgress()));
        }

        private boolean matches(Shortcut shortcut) {
            long now = System.currentTimeMillis();
            boolean matched = false;

            for (ShortcutSequence sequence : sequences) {
                SequenceProgress state = progress.get(sequence);
                if (state.nextStep > 0
                        && now - state.lastStepTimestamp > ShortcutSequence.DEFAULT_SEQUENCE_TIMEOUT_MILLIS) {
                    state.nextStep = 0;
                }

                if (sequence.steps().get(state.nextStep).equals(shortcut)) {
                    state.nextStep++;
                    state.lastStepTimestamp = now;
                    if (state.nextStep == sequence.steps().size()) {
                        state.nextStep = 0;
                        matched = true;
                    }
                } else if (sequence.steps().get(0).equals(shortcut)) {
                    state.nextStep = sequence.steps().size() == 1 ? 0 : 1;
                    state.lastStepTimestamp = now;
                    if (sequence.steps().size() == 1) {
                        matched = true;
                    }
                } else {
                    state.nextStep = 0;
                    state.lastStepTimestamp = 0;
                }
            }
            return matched;
        }
    }

    private static final class SequenceProgress {
        private int nextStep;
        private long lastStepTimestamp;
    }
}
