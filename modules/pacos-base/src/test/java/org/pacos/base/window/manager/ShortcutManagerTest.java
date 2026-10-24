package org.pacos.base.window.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.shortcut.Shortcut;
import org.pacos.base.window.shortcut.ShortcutSequence;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutEventListener;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.shared.Registration;

class ShortcutManagerTest {

    @Test
    void whenRegisterShortcutSequencesThenRegisterSingleStepAlternatives() {
        VaadinRequest previousRequest = VaadinRequest.getCurrent();
        VaadinRequest request = mock(VaadinRequest.class);
        CurrentInstance.set(VaadinRequest.class, request);
        TestShortcutManager shortcutManager = new TestShortcutManager();
        Shortcut controlShortcut = new Shortcut(Key.KEY_S, KeyModifier.CONTROL);

        try {
            ShortcutRegistration registration = shortcutManager.registerShortcutSequences(
                    mock(DesktopWindow.class),
                    List.of(new ShortcutSequence(controlShortcut)),
                    mock(ShortcutEventListener.class));

            assertEquals(List.of(controlShortcut, new Shortcut(Key.KEY_S, KeyModifier.META)),
                    shortcutManager.registeredShortcuts);
            assertSame(shortcutManager.registrations.get(0), registration);
        } finally {
            CurrentInstance.set(VaadinRequest.class, previousRequest);
        }
    }

    @Test
    void whenRegisterShortcutSequencesWithMultiStepSequenceThenThrowUnsupportedOperationException() {
        TestShortcutManager shortcutManager = new TestShortcutManager();
        ShortcutSequence sequence = new ShortcutSequence(
                new Shortcut(Key.KEY_C),
                new Shortcut(Key.KEY_S));
        DesktopWindow desktopWindow = mock(DesktopWindow.class);
        ShortcutEventListener shortcutEventListener = mock(ShortcutEventListener.class);
        List<ShortcutSequence> sequences = List.of(sequence);

        assertThrows(UnsupportedOperationException.class,
                () -> shortcutManager.registerShortcutSequences(desktopWindow, sequences, shortcutEventListener));

        assertEquals(List.of(), shortcutManager.registeredShortcuts);
    }

    @Test
    void whenRegisterSingleStepShortcutSequenceThenRemoveAllRegistrations() {
        VaadinRequest previousRequest = VaadinRequest.getCurrent();
        VaadinRequest request = mock(VaadinRequest.class);
        CurrentInstance.set(VaadinRequest.class, request);
        TestShortcutManager shortcutManager = new TestShortcutManager();

        try {
            Registration registration = shortcutManager.registerShortcutSequence(
                    mock(DesktopWindow.class),
                    new ShortcutSequence(new Shortcut(Key.KEY_S, KeyModifier.CONTROL)),
                    mock(ShortcutEventListener.class));

            assertEquals(2, shortcutManager.registrations.size());
            registration.remove();

            shortcutManager.registrations.forEach(reg -> verify(reg).remove());
        } finally {
            CurrentInstance.set(VaadinRequest.class, previousRequest);
        }
    }

    @Test
    void whenRegisterShortcutSequenceWithMultiStepSequenceThenThrowUnsupportedOperationException() {
        TestShortcutManager shortcutManager = new TestShortcutManager();
        DesktopWindow desktopWindow = mock(DesktopWindow.class);
        ShortcutSequence sequence = new ShortcutSequence(new Shortcut(Key.KEY_C), new Shortcut(Key.KEY_S));
        ShortcutEventListener shortcutEventListener = mock(ShortcutEventListener.class);

        assertThrows(UnsupportedOperationException.class,
                () -> shortcutManager.registerShortcutSequence(desktopWindow, sequence, shortcutEventListener));
        assertEquals(List.of(), shortcutManager.registeredShortcuts);
    }

    @Test
    void whenRegisterShortcutSequenceWithNullThenThrowNullPointerException() {
        TestShortcutManager shortcutManager = new TestShortcutManager();
        DesktopWindow desktopWindow = mock(DesktopWindow.class);
        ShortcutEventListener shortcutEventListener = mock(ShortcutEventListener.class);

        assertThrows(NullPointerException.class,
                () -> shortcutManager.registerShortcutSequence(desktopWindow, null, shortcutEventListener));
    }

    private static class TestShortcutManager implements ShortcutManager {
        private final List<Shortcut> registeredShortcuts = new ArrayList<>();
        private final List<ShortcutRegistration> registrations = new ArrayList<>();

        @Override
        public ShortcutRegistration registerShortcut(DesktopWindow desktopWindow, Shortcut shortcut,
                                                     ShortcutEventListener shortcutEventListener) {
            registeredShortcuts.add(shortcut);
            ShortcutRegistration registration = mock(ShortcutRegistration.class);
            registrations.add(registration);
            return registration;
        }

        @Override
        public void unregisterAll(DesktopWindow desktopWindow) {
        }

        @Override
        public void refreshShortcuts() {
        }
    }
}
