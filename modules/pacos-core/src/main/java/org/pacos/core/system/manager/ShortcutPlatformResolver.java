package org.pacos.core.system.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.KeyModifier;
import org.pacos.base.window.shortcut.Shortcut;

final class ShortcutPlatformResolver {

    private ShortcutPlatformResolver() {
    }

    static List<Shortcut> alternatives(Shortcut shortcut) {
        KeyModifier[] modifiers = shortcut.keyModifiers();
        boolean hasControl = false;
        boolean hasMeta = false;
        for (KeyModifier modifier : modifiers) {
            hasControl |= modifier == KeyModifier.CONTROL;
            hasMeta |= modifier == KeyModifier.META;
        }
        if (!hasControl) {
            return List.of(shortcut);
        }

        List<KeyModifier> macModifiers = new ArrayList<>(modifiers.length);
        boolean metaAdded = false;
        for (KeyModifier modifier : modifiers) {
            if (modifier == KeyModifier.CONTROL) {
                if (!hasMeta && !metaAdded) {
                    macModifiers.add(KeyModifier.META);
                    metaAdded = true;
                }
            } else {
                macModifiers.add(modifier);
            }
        }

        Shortcut macShortcut = new Shortcut(shortcut.key(), macModifiers.toArray(KeyModifier[]::new));
        return shortcut.equals(macShortcut) ? List.of(shortcut) : List.of(shortcut, macShortcut);
    }

    static Shortcut resolve(Shortcut shortcut, String userAgent) {
        List<Shortcut> alternatives = alternatives(shortcut);
        return isApplePlatform(userAgent) && alternatives.size() > 1
                ? alternatives.get(alternatives.size() - 1)
                : alternatives.get(0);
    }

    private static boolean isApplePlatform(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return false;
        }
        String normalizedUserAgent = userAgent.toLowerCase(Locale.ROOT);
        return normalizedUserAgent.contains("macintosh")
                || normalizedUserAgent.contains("mac os x")
                || normalizedUserAgent.contains("iphone")
                || normalizedUserAgent.contains("ipad")
                || normalizedUserAgent.contains("ipod");
    }
}
