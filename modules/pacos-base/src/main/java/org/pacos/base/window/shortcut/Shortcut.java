package org.pacos.base.window.shortcut;

import java.util.Arrays;
import java.util.Objects;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;

/**
 * Shortcut configuration handler
 */
public record Shortcut(Key key, KeyModifier... keyModifiers) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shortcut shortcut = (Shortcut) o;
        return Objects.equals(key, shortcut.key) && Arrays.equals(keyModifiers, shortcut.keyModifiers);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(key);
        result = 31 * result + Arrays.hashCode(keyModifiers);
        return result;
    }

    @Override
    public String toString() {
        return "Shortcut{" +
                "key=" + key +
                ", keyModifiers=" + Arrays.toString(keyModifiers) +
                '}';
    }
}
