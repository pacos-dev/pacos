package org.pacos.base.window.shortcut;

import org.junit.jupiter.api.Test;

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
}
