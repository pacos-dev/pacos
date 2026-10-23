package org.pacos.core.system.manager;

import java.util.Optional;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutEvent;
import com.vaadin.flow.component.ShortcutEventListener;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.event.UISystem;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.shortcut.Shortcut;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShortcutManagerImplTest {

    private static final String MAC_USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0) AppleWebKit/605.1.15";

    private ShortcutManagerImpl shortcutManager;
    private DesktopWindow mockWindow;
    private Shortcut mockShortcut;
    private ShortcutEventListener mockListener;
    private ShortcutEvent mockEvent;
    private UISystem uiSystem;

    @BeforeEach
    void setUp() {
        shortcutManager = new ShortcutManagerImpl();
        mockWindow = mock(DesktopWindow.class);
        mockShortcut = new Shortcut(Key.KEY_S, KeyModifier.CONTROL);
        mockListener = mock(ShortcutEventListener.class);
        mockEvent = mock(ShortcutEvent.class);

        uiSystem = VaadinMock.mockSystem();
        when(uiSystem.getWindowManager().getWindowDisplayedOnFront()).thenReturn(Optional.of(mockWindow));
    }

    @Test
    void whenRegisterShortcutThenShortcutShouldBeAdded() {
        ShortcutRegistration registration = mockShortcutRegistration();

        ShortcutRegistration result = shortcutManager.registerShortcut(mockWindow, mockShortcut, mockListener);

        assertNotNull(result);
        assertEquals(registration, result);
    }

    @Test
    void whenRegisterShortcutOnMacOSThenMetaModifierIsRegistered() {
        WebBrowser browser = mock(WebBrowser.class);
        doReturn(browser).when(VaadinSession.getCurrent()).getBrowser();
        when(browser.getUserAgent()).thenReturn(MAC_USER_AGENT);
        mockShortcutRegistration();

        shortcutManager.registerShortcut(mockWindow, mockShortcut, mockListener);

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        ArgumentCaptor<KeyModifier[]> modifiersCaptor = ArgumentCaptor.forClass(KeyModifier[].class);
        verify(UI.getCurrent()).addShortcutListener(any(ShortcutEventListener.class),
                keyCaptor.capture(), modifiersCaptor.capture());

        assertEquals(Key.KEY_S, keyCaptor.getValue());
        assertArrayEquals(new KeyModifier[]{KeyModifier.META}, modifiersCaptor.getValue());
    }

    @Test
    void whenShortcutDetectedThenListenerShouldBeInvoked() {
        shortcutManager.registerShortcut(mockWindow, mockShortcut, mockListener);

        shortcutManager.shortCutDetected(mockEvent, mockShortcut);

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
    void whenUnregisterAllThenListenersAndUnusedRegistrationShouldBeRemoved() {
        ShortcutRegistration registration = mockShortcutRegistration();
        shortcutManager.registerShortcut(mockWindow, mockShortcut, mockListener);

        shortcutManager.unregisterAll(mockWindow);
        shortcutManager.shortCutDetected(mockEvent, mockShortcut);

        verify(mockListener, never()).onShortcut(mockEvent);
        verify(registration).remove();
    }

    @Test
    void whenUnregisterAllThenKeepRegistrationWhileAnotherWindowUsesShortcut() {
        ShortcutRegistration registration = mockShortcutRegistration();
        DesktopWindow anotherWindow = mock(DesktopWindow.class);
        shortcutManager.registerShortcut(mockWindow, mockShortcut, mockListener);
        shortcutManager.registerShortcut(anotherWindow, mockShortcut, mockListener);

        shortcutManager.unregisterAll(mockWindow);

        verify(registration, never()).remove();

        shortcutManager.unregisterAll(anotherWindow);

        verify(registration).remove();
    }

    @Test
    void whenRefreshShortcutsThenRebindResolvedShortcutAndPreserveBrowserDefaults() {
        WebBrowser browser = mock(WebBrowser.class);
        doReturn(browser).when(VaadinSession.getCurrent()).getBrowser();
        when(browser.getUserAgent()).thenReturn(MAC_USER_AGENT);
        ShortcutRegistration firstRegistration = mock(ShortcutRegistration.class);
        ShortcutRegistration secondRegistration = mock(ShortcutRegistration.class);
        when(firstRegistration.isBrowserDefaultAllowed()).thenReturn(true);
        doReturn(firstRegistration, secondRegistration).when(UI.getCurrent())
                .addShortcutListener(any(ShortcutEventListener.class), any(Key.class), any(KeyModifier[].class));
        shortcutManager.registerShortcut(mockWindow, mockShortcut, mockListener);

        shortcutManager.refreshShortcuts();

        verify(firstRegistration).remove();
        verify(secondRegistration).setBrowserDefaultAllowed(true);
        ArgumentCaptor<KeyModifier[]> modifiersCaptor = ArgumentCaptor.forClass(KeyModifier[].class);
        verify(UI.getCurrent(), times(2)).addShortcutListener(any(ShortcutEventListener.class),
                any(Key.class), modifiersCaptor.capture());
        assertArrayEquals(new KeyModifier[]{KeyModifier.META}, modifiersCaptor.getAllValues().get(0));
        assertArrayEquals(new KeyModifier[]{KeyModifier.META}, modifiersCaptor.getAllValues().get(1));
    }

    private ShortcutRegistration mockShortcutRegistration() {
        ShortcutRegistration registration = mock(ShortcutRegistration.class);
        doReturn(registration).when(UI.getCurrent())
                .addShortcutListener(any(ShortcutEventListener.class), any(Key.class), any(KeyModifier[].class));
        return registration;
    }
}
