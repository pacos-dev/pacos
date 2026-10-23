package org.pacos.base.window.shortcut;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.vaadin.flow.server.VaadinRequest;

/**
 * Resolves platform-specific keyboard modifier alternatives before shortcut registration.
 */
public final class ShortcutPlatformResolver {

    private ShortcutPlatformResolver() {
    }

    public static List<ShortcutSequence> resolve(Collection<ShortcutSequence> definitions) {
        List<ShortcutSequence> candidates = ShortcutSequence.withMacOsAlternatives(definitions);
        return ShortcutSequence.forPlatform(candidates, isMacOs());
    }

    /**
     * Returns whether the current request comes from macOS or an Apple mobile device.
     */
    public static boolean isMacOs() {
        return isMacOsUserAgent(getUserAgent());
    }

    /**
     * Returns the conventional modifier label for the current client platform.
     */
    public static String getModifierLabel() {
        return isMacOs() ? "⌘" : "Ctrl";
    }

    static boolean isMacOsUserAgent(String userAgent) {
        if (userAgent == null) {
            return false;
        }
        String normalizedUserAgent = userAgent.toLowerCase(Locale.ROOT);
        return normalizedUserAgent.contains("macintosh")
                || normalizedUserAgent.contains("mac os x")
                || normalizedUserAgent.contains("iphone")
                || normalizedUserAgent.contains("ipad")
                || normalizedUserAgent.contains("ipod");
    }

    private static String getUserAgent() {
        VaadinRequest request = VaadinRequest.getCurrent();
        return request == null ? null : request.getHeader("User-Agent");
    }
}
