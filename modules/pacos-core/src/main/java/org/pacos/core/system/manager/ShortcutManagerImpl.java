package org.pacos.core.system.manager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.vaadin.flow.component.ShortcutEvent;
import com.vaadin.flow.component.ShortcutEventListener;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.UI;
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
        registeredShortcuts.computeIfAbsent(shortcut,
                s -> UI.getCurrent().addShortcutListener(e -> shortCutDetected(e, s), s.key(), s.keyModifiers()));

        shortcutListenerMap.computeIfAbsent(desktopWindow, e -> new HashSet<>()).add(
                new ShortcutListener(shortcut, shortcutEventListener));
        return registeredShortcuts.get(shortcut);
    }

    @Override
    public void unregisterAll(DesktopWindow desktopWindow) {
        shortcutListenerMap.remove(desktopWindow);
    }

    @Override
    public void refreshShortcuts() {
        registeredShortcuts.keySet().forEach(s ->
                UI.getCurrent().addShortcutListener(e -> shortCutDetected(e, s), s.key(), s.keyModifiers()));
    }

    void shortCutDetected(ShortcutEvent event, Shortcut shortcut) {
        Optional<DesktopWindow> activeWindow = UISystem.getCurrent().getWindowManager().getWindowDisplayedOnFront();
        if (activeWindow.isPresent()) {
            Set<ShortcutListener> listeners = shortcutListenerMap.get(activeWindow.get());
            if (listeners == null) {
                return;
            }
            listeners.stream().filter(l -> l.shortcut().equals(shortcut)).findFirst()
                    .ifPresent(shortcutListener ->
                            shortcutListener.listener().onShortcut(event));
        }
    }
}
