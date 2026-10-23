package org.pacos.base.window.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.ShortcutEventListener;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.shared.Registration;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.shortcut.Shortcut;
import org.pacos.base.window.shortcut.ShortcutSequence;
import org.pacos.base.window.shortcut.ShortcutPlatformResolver;

/**
 * It is responsible for handling keyboard shortcuts on the client side.
 * The listener is strongly associated with the window and can only be called if the window
 * is active at the time the event is registered.
 */
public interface ShortcutManager extends Serializable {
    /**
     * Register listener for given window
     */
    ShortcutRegistration registerShortcut(DesktopWindow desktopWindow, Shortcut shortcut,
            ShortcutEventListener shortcutEventListener);

    /**
     * Registers alternative shortcut sequences for a single logical action.
     *
     * <p>The default implementation preserves compatibility with existing manager implementations
     * and supports alternatives consisting of single key presses. Implementations that support
     * multi-step sequences can override this method.</p>
     */
    default ShortcutRegistration registerShortcutSequences(DesktopWindow desktopWindow,
            List<ShortcutSequence> sequences, ShortcutEventListener shortcutEventListener) {
        List<ShortcutSequence> alternatives = ShortcutPlatformResolver.resolve(sequences);
        if (alternatives.stream().anyMatch(sequence -> sequence.steps().size() != 1)) {
            throw new UnsupportedOperationException("This shortcut manager does not support multi-step sequences");
        }

        ShortcutRegistration firstRegistration = null;
        for (ShortcutSequence sequence : alternatives) {
            ShortcutRegistration registration = registerShortcut(desktopWindow, sequence.steps().get(0),
                    shortcutEventListener);
            if (firstRegistration == null) {
                firstRegistration = registration;
            }
        }
        return firstRegistration;
    }

    /**
     * Registers a multi-step sequence. The returned registration removes the complete sequence.
     *
     * <p>The default implementation supports single-step sequences so implementations of this
     * interface remain source and binary compatible. Multi-step support is provided by the core manager.</p>
     */
    default Registration registerShortcutSequence(DesktopWindow desktopWindow, ShortcutSequence sequence,
            ShortcutEventListener shortcutEventListener) {
        Objects.requireNonNull(sequence, "sequence");
        List<ShortcutSequence> alternatives = ShortcutPlatformResolver.resolve(List.of(sequence));
        if (alternatives.stream().anyMatch(candidate -> candidate.steps().size() != 1)) {
            throw new UnsupportedOperationException("This shortcut manager does not support multi-step sequences");
        }

        List<ShortcutRegistration> registrations = new ArrayList<>();
        for (ShortcutSequence alternative : alternatives) {
            registrations.add(registerShortcut(desktopWindow, alternative.steps().get(0), shortcutEventListener));
        }
        return () -> registrations.forEach(ShortcutRegistration::remove);
    }

    /**
     * Unregister listener for the given window
     */
    void unregisterAll(DesktopWindow desktopWindow);

    /**
     * Refresh all shortcuts listeners
     */
    void refreshShortcuts();
}
