package org.pacos.core.system.manager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
    void whenRegisteredShortcutIsTriggeredThenDispatchToWindowListener() {
        Shortcut shortcut = new Shortcut(Key.KEY_S);
        ShortcutRegistration registration = mock(ShortcutRegistration.class);
        ArgumentCaptor<ShortcutEventListener> listenerCaptor =
                ArgumentCaptor.forClass(ShortcutEventListener.class);
        doReturn(registration).when(UI.getCurrent()).addShortcutListener(
                any(ShortcutEventListener.class), eq(Key.KEY_S), any(KeyModifier[].class));

        shortcutManager.registerShortcut(mockWindow, shortcut, mockListener);
        verify(UI.getCurrent()).addShortcutListener(
                listenerCaptor.capture(),
                eq(Key.KEY_S),
                any(KeyModifier[].class));
        listenerCaptor.getValue().onShortcut(mockEvent);

        verify(mockListener).onShortcut(mockEvent);
    }

    @Test
    void whenShortcutDetectedWithoutActiveWindowThenListenerShouldNotBeInvoked() {
        when(UISystem.getCurrent().getWindowManager().getWindowDisplayedOnFront()).thenReturn(Optional.empty());
        shortcutManager.registerShortcut(mockWindow, mockShortcut, mockListener);

        shortcutManager.shortCutDetected(mockEvent, mockShortcut);

        verify(mockListener, never()).onShortcut(mockEvent);
    }

    @Test
    void whenShortcutDetectedWithoutRegisteredListenersThenListenerShouldNotBeInvoked() {
        shortcutManager.shortCutDetected(mockEvent, mockShortcut);

        verify(mockListener, never()).onShortcut(mockEvent);
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
    void whenShortcutSequenceIsRestartedAfterMismatchThenInvokeAfterCompletion() {
        Shortcut firstStep = new Shortcut(Key.KEY_C, KeyModifier.CONTROL);
        Shortcut secondStep = new Shortcut(Key.KEY_S, KeyModifier.CONTROL);
        ShortcutSequence sequence = new ShortcutSequence(firstStep, secondStep);
        shortcutManager.registerShortcutSequence(mockWindow, sequence, mockListener);

        shortcutManager.shortCutDetected(mockEvent, firstStep);
        shortcutManager.shortCutDetected(mockEvent, new Shortcut(Key.KEY_X, KeyModifier.CONTROL));
        shortcutManager.shortCutDetected(mockEvent, secondStep);
        verify(mockListener, never()).onShortcut(mockEvent);

        shortcutManager.shortCutDetected(mockEvent, firstStep);
        shortcutManager.shortCutDetected(mockEvent, firstStep);
        shortcutManager.shortCutDetected(mockEvent, secondStep);

        verify(mockListener, times(1)).onShortcut(mockEvent);
    }

    @Test
    void whenShortcutSequenceTimesOutThenRestartSequenceProgress() throws ReflectiveOperationException {
        Shortcut firstStep = new Shortcut(Key.KEY_C, KeyModifier.CONTROL);
        Shortcut secondStep = new Shortcut(Key.KEY_S, KeyModifier.CONTROL);
        ShortcutSequence sequence = new ShortcutSequence(firstStep, secondStep);
        shortcutManager.registerShortcutSequence(mockWindow, sequence, mockListener);
        shortcutManager.shortCutDetected(mockEvent, firstStep);

        Field listenersField = ShortcutManagerImpl.class.getDeclaredField("shortcutListenerMap");
        listenersField.setAccessible(true);
        Map<?, ?> listenersByWindow = (Map<?, ?>) listenersField.get(shortcutManager);
        Object sequenceListener = ((Set<?>) listenersByWindow.get(mockWindow)).iterator().next();
        Field progressField = sequenceListener.getClass().getDeclaredField("progress");
        progressField.setAccessible(true);
        Map<?, ?> progressBySequence = (Map<?, ?>) progressField.get(sequenceListener);
        Object progress = progressBySequence.get(sequence);
        Field lastStepTimestampField = progress.getClass().getDeclaredField("lastStepTimestamp");
        lastStepTimestampField.setAccessible(true);
        lastStepTimestampField.setLong(progress, 0);

        shortcutManager.shortCutDetected(mockEvent, secondStep);
        verify(mockListener, never()).onShortcut(mockEvent);
        shortcutManager.shortCutDetected(mockEvent, firstStep);
        shortcutManager.shortCutDetected(mockEvent, secondStep);

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

    @Test
    void whenRegisterShortcutSequencesThenReturnRegistrationForFirstShortcut() {
        Shortcut firstShortcut = new Shortcut(Key.KEY_C);
        Shortcut secondShortcut = new Shortcut(Key.KEY_S);
        ShortcutRegistration firstRegistration = mock(ShortcutRegistration.class);
        ShortcutRegistration secondRegistration = mock(ShortcutRegistration.class);
        doReturn(firstRegistration, secondRegistration).when(UI.getCurrent()).addShortcutListener(
                any(ShortcutEventListener.class), any(Key.class), any(KeyModifier[].class));

        ShortcutRegistration result = shortcutManager.registerShortcutSequences(
                mockWindow,
                List.of(new ShortcutSequence(firstShortcut), new ShortcutSequence(secondShortcut)),
                mockListener);

        assertSame(firstRegistration, result);
    }

    @Test
    void whenShortcutIsSharedBetweenWindowsThenKeepRegistrationUntilLastWindowIsUnregistered() {
        DesktopWindow anotherWindow = mock(DesktopWindow.class);
        Shortcut shortcut = new Shortcut(Key.KEY_S);
        ShortcutRegistration registration = mock(ShortcutRegistration.class);
        doReturn(registration).when(UI.getCurrent()).addShortcutListener(
                any(ShortcutEventListener.class), any(Key.class), any(KeyModifier[].class));
        shortcutManager.registerShortcut(mockWindow, shortcut, mockListener);
        shortcutManager.registerShortcut(anotherWindow, shortcut, mockListener);

        shortcutManager.unregisterAll(mockWindow);
        verify(registration, never()).remove();

        shortcutManager.unregisterAll(anotherWindow);
        verify(registration).remove();
    }

    @Test
    void whenShortcutSequenceIsRemovedThenKeepSharedShortcutUntilLastListenerIsRemoved() {
        Shortcut shortcut = new Shortcut(Key.KEY_S);
        ShortcutRegistration registration = mock(ShortcutRegistration.class);
        doReturn(registration).when(UI.getCurrent()).addShortcutListener(
                any(ShortcutEventListener.class), any(Key.class), any(KeyModifier[].class));
        Registration firstRegistration = shortcutManager.registerShortcutSequence(
                mockWindow, new ShortcutSequence(shortcut), mockListener);
        Registration secondRegistration = shortcutManager.registerShortcutSequence(
                mockWindow, new ShortcutSequence(shortcut), mockListener);

        shortcutManager.shortCutDetected(mockEvent, shortcut);
        verify(mockListener).onShortcut(mockEvent);

        firstRegistration.remove();
        verify(registration, never()).remove();

        secondRegistration.remove();
        verify(registration).remove();

        secondRegistration.remove();
        verify(registration).remove();
    }

    @Test
    void whenRefreshShortcutsThenReplaceRegisteredListeners() {
        Shortcut shortcut = new Shortcut(Key.KEY_S);
        ShortcutRegistration oldRegistration = mock(ShortcutRegistration.class);
        ShortcutRegistration refreshedRegistration = mock(ShortcutRegistration.class);
        doReturn(oldRegistration, refreshedRegistration).when(UI.getCurrent()).addShortcutListener(
                any(ShortcutEventListener.class), any(Key.class), any(KeyModifier[].class));
        shortcutManager.registerShortcut(mockWindow, shortcut, mockListener);

        shortcutManager.refreshShortcuts();

        verify(oldRegistration).remove();
        verify(UI.getCurrent(), times(2)).addShortcutListener(
                any(ShortcutEventListener.class), any(Key.class), any(KeyModifier[].class));
    }

    @Test
    void whenRefreshFindsShortcutWithoutRegistrationThenRegisterItAgain() throws ReflectiveOperationException {
        Shortcut shortcut = new Shortcut(Key.KEY_S);
        ShortcutRegistration registration = mock(ShortcutRegistration.class);
        doReturn(registration).when(UI.getCurrent()).addShortcutListener(
                any(ShortcutEventListener.class), any(Key.class), any(KeyModifier[].class));
        addNullShortcutRegistration(shortcut);

        shortcutManager.refreshShortcuts();

        verify(registration, never()).remove();
        verify(UI.getCurrent()).addShortcutListener(
                any(ShortcutEventListener.class), eq(Key.KEY_S), any(KeyModifier[].class));
    }

    @Test
    void whenUnregisterUnusedShortcutHasNoRegistrationThenIgnore() throws ReflectiveOperationException {
        Shortcut shortcut = new Shortcut(Key.KEY_S);
        addNullShortcutRegistration(shortcut);

        shortcutManager.unregisterAll(mockWindow);

        // No exception should be thrown when unregistering a shortcut without a registration.
        assertNotNull(shortcutManager);
    }

    private void addNullShortcutRegistration(Shortcut shortcut) throws ReflectiveOperationException {
        Field field = ShortcutManagerImpl.class.getDeclaredField("registeredShortcuts");
        field.setAccessible(true);
        Object registeredShortcuts = field.get(shortcutManager);
        registeredShortcuts.getClass().getMethod("put", Object.class, Object.class)
                .invoke(registeredShortcuts, shortcut, null);
    }
}
