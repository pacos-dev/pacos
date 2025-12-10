package org.pacos.core.component.variable.system.global;

import org.pacos.base.event.EventType;

/**
 * Collected all possible event for variable module
 */
public enum GlobalVariableEvent implements EventType {

    OPEN_GLOBAL_VARIABLE_TAB,
    REFRESH_ENTRY,
    ADD_NEW_VARIABLE_TO_GRID,
    REMOVED_ENTRY;

    GlobalVariableEvent() {
    }
}
