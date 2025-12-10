package org.pacos.core.component.dock.view.settings;

import java.util.List;

import org.pacos.base.window.DesktopWindow;

/**
 * Contains lists of module activator that extends {@link DesktopWindow}
 *
 * @param activators
 */
public record ActivatorConfig(List<String> activators) {
}
