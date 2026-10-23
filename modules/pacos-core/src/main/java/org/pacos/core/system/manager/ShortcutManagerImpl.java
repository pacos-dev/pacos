package org.pacos.core.system.manager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.component.ShortcutEvent;
import com.vaadin.flow.component.ShortcutEventListener;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import org.pacos.base.event.UISystem;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.manager.ShortcutManager;
import org.pacos.base.window.shortcut.Shortcut;

public class ShortcutManagerImpl implements ShortcutManager, Serializable {
    private final Map<DesktopWindow, Set<ShortcutListener>> shortcutListenerMap = new HashMap<>();
    private final Map<Shortcut, ShortcutRegistration> registeredShortcuts = new HashMap<>();

    @Override
    public ShortcutRegistration registerShortcut(DesktopWindow desktopWindow, Shortcut shortcut,
                                                 ShortcutEventListener shortcutEventListener) {
        registeredShortcuts.computeIfAbsent(shortcut, s -> createRegistration(s, null));

        shortcutListenerMap.computeIfAbsent(desktopWindow, e -> new HashSet<>()).add(
                new ShortcutListener(shortcut, shortcutEventListener));
        return registeredShortcuts.get(shortcut);
    }

    @Override
    public void unregisterAll(DesktopWindow desktopWindow) {
        Set<ShortcutListener> removedListeners = shortcutListenerMap.remove(desktopWindow);
        if (removedListeners == null) {
            return;
        }

        Set<Shortcut> shortcuts = removedListeners.stream()
                .map(ShortcutListener::shortcut)
                .collect(Collectors.toSet());
        for (Shortcut shortcut : shortcuts) {
            if (!isShortcutUsed(shortcut)) {
                ShortcutRegistration registration = registeredShortcuts.remove(shortcut);
                if (registration != null) {
                    registration.remove();
                }
            }
        }
    }

    @Override
    public void refreshShortcuts() {
        Map<Shortcut, ShortcutRegistration> previousRegistrations = new HashMap<>(registeredShortcuts);
        registeredShortcuts.clear();
        previousRegistrations.forEach((shortcut, previous) -> {
            ShortcutRegistrationSettings settings = ShortcutRegistrationSettings.from(previous);
            previous.remove();
            registeredShortcuts.put(shortcut, createRegistration(shortcut, settings));
        });
    }

    void shortCutDetected(ShortcutEvent event, Shortcut shortcut) {
        Optional<DesktopWindow> activeWindow = UISystem.getCurrent().getWindowManager().getWindowDisplayedOnFront();
        if (activeWindow.isPresent()) {
            Set<ShortcutListener> listeners = shortcutListenerMap.get(activeWindow.get());
            if (listeners == null) {
                return;
            }
            listeners.stream().filter(l -> l.shortcut().equals(shortcut)).findFirst()
                    .ifPresent(shortcutListener -> shortcutListener.listener().onShortcut(event));
        }
    }

    private ShortcutRegistration createRegistration(Shortcut logicalShortcut, ShortcutRegistrationSettings settings) {
        Shortcut resolvedShortcut = ShortcutPlatformResolver.resolve(logicalShortcut, getCurrentUserAgent());
        ShortcutRegistration registration = UI.getCurrent().addShortcutListener(
                event -> shortCutDetected(event, logicalShortcut),
                resolvedShortcut.key(),
                resolvedShortcut.keyModifiers());
        if (settings != null) {
            settings.applyTo(registration);
        }
        return registration;
    }

    private String getCurrentUserAgent() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return null;
        }
        WebBrowser browser = session.getBrowser();
        return browser == null ? null : browser.getUserAgent();
    }

    private boolean isShortcutUsed(Shortcut shortcut) {
        return shortcutListenerMap.values().stream()
                .flatMap(Set::stream)
                .anyMatch(listener -> listener.shortcut().equals(shortcut));
    }

    private record ShortcutRegistrationSettings(
            boolean browserDefaultAllowed,
            boolean eventPropagationAllowed,
            boolean eventsFromNestedModalsAllowed,
            boolean resetFocusOnActiveElement,
            DisabledUpdateMode disabledUpdateMode) {

        private static ShortcutRegistrationSettings from(ShortcutRegistration registration) {
            return new ShortcutRegistrationSettings(
                    registration.isBrowserDefaultAllowed(),
                    registration.isEventPropagationAllowed(),
                    registration.isEventsFromNestedModalsAllowed(),
                    registration.isResetFocusOnActiveElement(),
                    registration.getDisabledUpdateMode());
        }

        private void applyTo(ShortcutRegistration registration) {
            registration.setBrowserDefaultAllowed(browserDefaultAllowed);
            registration.setEventPropagationAllowed(eventPropagationAllowed);
            registration.setEventsFromNestedModalsAllowed(eventsFromNestedModalsAllowed);
            registration.setResetFocusOnActiveElement(resetFocusOnActiveElement);
            if (disabledUpdateMode != null) {
                registration.setDisabledUpdateMode(disabledUpdateMode);
            }
        }
    }
}
