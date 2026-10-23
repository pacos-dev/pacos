package org.pacos.core.system.manager;

import java.util.List;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import org.junit.jupiter.api.Test;
import org.pacos.base.window.shortcut.Shortcut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ShortcutPlatformResolverTest {

    private static final String MAC_USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0) AppleWebKit/605.1.15";
    private static final String WINDOWS_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    @Test
    void whenResolveMacShortcutThenControlModifierBecomesMeta() {
        Shortcut controlShortcut = new Shortcut(Key.KEY_S, KeyModifier.CONTROL);

        Shortcut result = ShortcutPlatformResolver.resolve(controlShortcut, MAC_USER_AGENT);

        assertEquals(new Shortcut(Key.KEY_S, KeyModifier.META), result);
    }

    @Test
    void whenResolveWindowsShortcutThenControlModifierRemainsControl() {
        Shortcut controlShortcut = new Shortcut(Key.KEY_S, KeyModifier.CONTROL);

        Shortcut result = ShortcutPlatformResolver.resolve(controlShortcut, WINDOWS_USER_AGENT);

        assertEquals(controlShortcut, result);
    }

    @Test
    void whenGetAlternativesForControlShortcutThenReturnControlAndMetaVariants() {
        Shortcut controlShortcut = new Shortcut(Key.KEY_C, KeyModifier.CONTROL);

        List<Shortcut> result = ShortcutPlatformResolver.alternatives(controlShortcut);

        assertEquals(List.of(
                controlShortcut,
                new Shortcut(Key.KEY_C, KeyModifier.META)), result);
    }

    @Test
    void whenResolveMacShortcutWithShiftThenPreserveOtherModifiers() {
        Shortcut shortcut = new Shortcut(Key.KEY_S, KeyModifier.CONTROL, KeyModifier.SHIFT);

        Shortcut result = ShortcutPlatformResolver.resolve(shortcut, MAC_USER_AGENT);

        assertEquals(new Shortcut(Key.KEY_S, KeyModifier.META, KeyModifier.SHIFT), result);
    }

    @Test
    void whenResolveShortcutWithoutControlThenReturnOriginalDefinition() {
        Shortcut shortcut = new Shortcut(Key.DELETE);

        Shortcut result = ShortcutPlatformResolver.resolve(shortcut, MAC_USER_AGENT);

        assertSame(shortcut, result);
    }

    @Test
    void whenResolveShortcutWithoutUserAgentThenKeepControlModifier() {
        Shortcut shortcut = new Shortcut(Key.KEY_C, KeyModifier.CONTROL);

        Shortcut result = ShortcutPlatformResolver.resolve(shortcut, null);

        assertEquals(shortcut, result);
    }
}
