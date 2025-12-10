package org.pacos.core.component.plugin.event;

/**
 * Triggered when user confirms plugin deletion
 */
@FunctionalInterface
public interface OnRemoveFinishEvent {
    void finish();
}
