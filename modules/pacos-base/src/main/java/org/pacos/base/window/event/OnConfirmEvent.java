package org.pacos.base.window.event;

/**
 * Used by modal window to trigger event after button 'Ok' clicked
 */
@FunctionalInterface
public interface OnConfirmEvent {
    /**
     * Return true if modal can by close immediately, or false otherwise
     */
    boolean confirm();
}
