package org.pacos.core.system.manager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.event.UISystem;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.shortcut.Shortcut;
import org.pacos.base.window.shortcut.ShortcutSequence;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutEvent;
import com.vaadin.flow.component.ShortcutEventListener;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.shared.Registration;

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
    void whenRegisterShortcutThenAllowPropagationAndNestedModalEvents() {
        ShortcutRegistration registration = mock(ShortcutRegistration.class);
        doReturn(registration).when(UI.getCurrent()).addShortcutListener(
                any(ShortcutEventListener.class),
                any(Key.class),
                any(KeyModifier[].class));

        shortcutManager.registerShortcut(mockWindow, mockShortcut, mockListener);

        verify(registration, times(2)).setEventPropagationAllowed(true);
        verify(registration, times(2)).setEventsFromNestedModalsAllowed(true);
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
    void whenShortcutSequenceRegisteredOnMacOsThenInvokeListenerForMetaSequence() {
        VaadinRequest request = mock(VaadinRequest.class);
        when(request.getHeader("User-Agent"))
                .thenReturn("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15");
        CurrentInstance.set(VaadinRequest.class, request);
        ShortcutSequence sequence = new ShortcutSequence(
                new Shortcut(Key.KEY_C, KeyModifier.CONTROL),
                new Shortcut(Key.KEY_S, KeyModifier.CONTROL));

        try {
            shortcutManager.registerShortcutSequence(mockWindow, sequence, mockListener);

            shortcutManager.shortCutDetected(mockEvent, new Shortcut(Key.KEY_C, KeyModifier.META));
            shortcutManager.shortCutDetected(mockEvent, new Shortcut(Key.KEY_S, KeyModifier.META));

            verify(mockListener, times(1)).onShortcut(mockEvent);
        } finally {
            CurrentInstance.set(VaadinRequest.class, null);
        }
    }

    @Test
    void whenShortcutSequenceRegisteredOnWindowsThenIgnoreMetaSequence() {
        VaadinRequest request = mock(VaadinRequest.class);
        when(request.getHeader("User-Agent"))
                .thenReturn("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        CurrentInstance.set(VaadinRequest.class, request);
        ShortcutSequence sequence = new ShortcutSequence(
                new Shortcut(Key.KEY_C, KeyModifier.CONTROL),
                new Shortcut(Key.KEY_S, KeyModifier.CONTROL));

        try {
            shortcutManager.registerShortcutSequence(mockWindow, sequence, mockListener);

            shortcutManager.shortCutDetected(mockEvent, new Shortcut(Key.KEY_C, KeyModifier.META));
            shortcutManager.shortCutDetected(mockEvent, new Shortcut(Key.KEY_S, KeyModifier.META));

            verify(mockListener, never()).onShortcut(mockEvent);
        } finally {
            CurrentInstance.set(VaadinRequest.class, null);
        }
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
    void whenRegisterControlShortcutOnMacOsThenInvokeListenerForMetaAlternative() {
        VaadinRequest request = mock(VaadinRequest.class);
        when(request.getHeader("User-Agent"))
                .thenReturn("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15");
        CurrentInstance.set(VaadinRequest.class, request);

        try {
            shortcutManager.registerShortcut(mockWindow, mockShortcut, mockListener);

            shortcutManager.shortCutDetected(mockEvent, new Shortcut(Key.KEY_S, KeyModifier.META));

            verify(mockListener, times(1)).onShortcut(mockEvent);
        } finally {
            CurrentInstance.set(VaadinRequest.class, null);
        }
    }

    @Test
    void whenPlatformCannotBeDetectedThenAcceptMetaShortcut() {
        VaadinRequest previousRequest = VaadinRequest.getCurrent();
        CurrentInstance.set(VaadinRequest.class, null);

        try {
            shortcutManager.registerShortcut(mockWindow, mockShortcut, mockListener);

            shortcutManager.shortCutDetected(mockEvent, new Shortcut(Key.KEY_S, KeyModifier.META));

            verify(mockListener, times(1)).onShortcut(mockEvent);
        } finally {
            CurrentInstance.set(VaadinRequest.class, previousRequest);
        }
    }

    @Test
    void whenShortcutDetectedOnDifferentWindowThenListenerShouldNotBeInvoked() {
        DesktopWindow anotherWindow = mock(DesktopWindow.class);
        shortcutManager.registerShortcut(anotherWindow, mockShortcut, mockListener);

        shortcutManager.shortCutDetected(mockEvent, mockShortcut);

        verify(mockListener, never()).onShortcut(mockEvent);
    }

    @Test
    void whenUnregisterAllThenSequenceListenerShouldBeRemoved() {
        ShortcutSequence sequence = new ShortcutSequence(
                new Shortcut(Key.KEY_C, KeyModifier.CONTROL),
                new Shortcut(Key.KEY_S, KeyModifier.CONTROL));
        shortcutManager.registerShortcutSequence(mockWindow, sequence, mockListener);

        shortcutManager.unregisterAll(mockWindow);

        shortcutManager.shortCutDetected(mockEvent, new Shortcut(Key.KEY_C, KeyModifier.CONTROL));
        shortcutManager.shortCutDetected(mockEvent, new Shortcut(Key.KEY_S, KeyModifier.CONTROL));

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
