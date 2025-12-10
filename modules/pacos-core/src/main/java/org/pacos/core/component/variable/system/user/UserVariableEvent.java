package org.pacos.core.component.variable.system.user;

import org.pacos.base.event.EventType;

/**
 * Collected all possible event for variable module
 */
public enum UserVariableEvent implements EventType {

    OPEN_COLLECTION_VARIABLE_TAB,
    ADD_NEW_COLLECTION_TO_GRID,
    CLOSE_ALL_TABS,
    COLLECTION_REMOVED,
    CLOSE_TAB,
    REFRESH_COLLECTION_NAME,
    REFRESH_COLLECTION_LIST, //refresh collection list in variable plugin
    REFRESH_VARIABLE_LIST,
    REFRESH_VARIABLE, // refresh variables in variable plugin
    SAVE_SHORTCUT_EVENT,
    DELETE_SHORTCUT_EVENT,
    COPY_SHORTCUT_EVENT,
    PASTE_SHORTCUT_EVENT;

    UserVariableEvent() {
    }
}
