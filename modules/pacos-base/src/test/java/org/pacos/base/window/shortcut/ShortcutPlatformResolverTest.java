package org.pacos.base.window.shortcut;

import java.util.List;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.VaadinRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShortcutPlatformResolverTest {

    @Test
    void whenMacOsUserAgentThenReturnTrue() {
        assertTrue(ShortcutPlatformResolver.isMacOsUserAgent(
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15"));
    }

    @Test
    void whenWindowsUserAgentThenReturnFalse() {
        assertFalse(ShortcutPlatformResolver.isMacOsUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"));
    }

    @Test
    void whenUserAgentIsMissingThenReturnFalse() {
        assertFalse(ShortcutPlatformResolver.isMacOsUserAgent(null));
    }

    @Test
    void whenPlatformCannotBeDetectedThenResolveControlAndMetaAlternatives() {
        VaadinRequest previousRequest = VaadinRequest.getCurrent();
        CurrentInstance.set(VaadinRequest.class, null);

        try {
            ShortcutSequence controlShortcut = new ShortcutSequence(
                    new Shortcut(Key.KEY_C, KeyModifier.CONTROL));
            ShortcutSequence metaShortcut = new ShortcutSequence(
                    new Shortcut(Key.KEY_C, KeyModifier.META));

            List<ShortcutSequence> resolved = ShortcutPlatformResolver.resolve(List.of(controlShortcut));

            assertTrue(resolved.contains(controlShortcut));
            assertTrue(resolved.contains(metaShortcut));
            assertEquals("Ctrl / ⌘", ShortcutPlatformResolver.getModifierLabel());
        } finally {
            CurrentInstance.set(VaadinRequest.class, previousRequest);
        }
    }
}
