package org.pacos.core.system.manager;

import java.util.Optional;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutEvent;
import com.vaadin.flow.component.ShortcutEventListener;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.shared.Registration;
import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.event.UISystem;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.shortcut.Shortcut;
import org.pacos.base.window.shortcut.ShortcutSequence;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShortcutManagerImplTest {

    private ShortcutManagerImpl shortcutManager;
    private DesktopWindow mockWindow;
    private Shortcut mockShortcut;
    private ShortcutEventListener mockListener;
    private ShortcutEvent mockEvent;

    @BeforeEach
    void setUp() {
        shortcutManager = new ShortcutManagerImpl();
        mockWindow = mock(DesktopWindow.class);
        mockShortcut = new Shortcut(Key.KEY_S, KeyModifier.CONTROL);
        mockListener = mock(ShortcutEventListener.class);
        mockEvent = mock(ShortcutEvent.class);

        // Mock UISystem behavior
        UISystem mockUISystem = VaadinMock.mockSystem();
        when(mockUISystem.getWindowManager().getWindowDisplayedOnFront()).thenReturn(Optional.of(mockWindow));
    }

    @Test
    void whenRegisterShortcutThenShortcutShouldBeAdded() {
        ShortcutRegistration registration = shortcutManager.registerShortcut(mockWindow, mockShortcut, mockListener);
        assertNotNull(registration);
    }

    @Test
    void whenShortcutDetectedThenListenerShouldBeInvoked() {
        shortcutManager.registerShortcut(mockWindow, mockShortcut, mockListener);

        shortcutManager.shortCutDetected(mockEvent, mockShortcut);

        verify(mockListener, times(1)).onShortcut(mockEvent);
    }

    @Test
    void whenShortcutSequenceThenInvokeListenerOnlyAfterLastStep() {
        ShortcutSequence sequence = new ShortcutSequence(
                new Shortcut(Key.KEY_C, KeyModifier.CONTROL),
                new Shortcut(Key.KEY_S, KeyModifier.CONTROL));
        shortcutManager.registerShortcutSequence(mockWindow, sequence, mockListener);

        shortcutManager.shortCutDetected(mockEvent, new Shortcut(Key.KEY_C, KeyModifier.CONTROL));

        verify(mockListener, never()).onShortcut(mockEvent);

        shortcutManager.shortCutDetected(mockEvent, new Shortcut(Key.KEY_S, KeyModifier.CONTROL));

        verify(mockListener, times(1)).onShortcut(mockEvent);
    }

    @Test
    void whenShortcutSequenceThenInvokeListenerForMacMetaAlternative() {
        ShortcutSequence sequence = new ShortcutSequence(
                new Shortcut(Key.KEY_C, KeyModifier.CONTROL),
                new Shortcut(Key.KEY_S, KeyModifier.CONTROL));
        shortcutManager.registerShortcutSequence(mockWindow, sequence, mockListener);

        shortcutManager.shortCutDetected(mockEvent, new Shortcut(Key.KEY_C, KeyModifier.META));
        shortcutManager.shortCutDetected(mockEvent, new Shortcut(Key.KEY_S, KeyModifier.META));

        verify(mockListener, times(1)).onShortcut(mockEvent);
    }

    @Test
    void whenShortcutSequenceIsRemovedThenDoNotInvokeListener() {
        ShortcutSequence sequence = new ShortcutSequence(
                new Shortcut(Key.KEY_C, KeyModifier.CONTROL),
                new Shortcut(Key.KEY_S, KeyModifier.CONTROL));
        Registration registration = shortcutManager.registerShortcutSequence(mockWindow, sequence, mockListener);
        registration.remove();

        shortcutManager.shortCutDetected(mockEvent, new Shortcut(Key.KEY_C, KeyModifier.CONTROL));
        shortcutManager.shortCutDetected(mockEvent, new Shortcut(Key.KEY_S, KeyModifier.CONTROL));

        verify(mockListener, never()).onShortcut(mockEvent);
    }

    @Test
    void whenRegisterControlShortcutThenInvokeListenerForMacMetaAlternative() {
        shortcutManager.registerShortcut(mockWindow, mockShortcut, mockListener);

        shortcutManager.shortCutDetected(mockEvent, new Shortcut(Key.KEY_S, KeyModifier.META));

        verify(mockListener, times(1)).onShortcut(mockEvent);
    }

    @Test
    void whenShortcutDetectedOnDifferentWindowThenListenerShouldNotBeInvoked() {
        DesktopWindow anotherWindow = mock(DesktopWindow.class);
        shortcutManager.registerShortcut(anotherWindow, mockShortcut, mockListener);

        shortcutManager.shortCutDetected(mockEvent, mockShortcut);

        verify(mockListener, never()).onShortcut(mockEvent);
    }

    @Test
    void whenUnregisterAllThenListenersShouldBeRemoved() {
        shortcutManager.registerShortcut(mockWindow, mockShortcut, mockListener);
        shortcutManager.unregisterAll(mockWindow);

        shortcutManager.shortCutDetected(mockEvent, mockShortcut);

        verify(mockListener, never()).onShortcut(mockEvent);
    }
}
