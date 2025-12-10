package org.pacos.core.component.variable.event.user;

import org.pacos.core.component.variable.view.user.UserVariableForm;

public final class FormChangedEvent {
    private FormChangedEvent() {
    }

    public static void fireEvent(Boolean changed, UserVariableForm userVariableForm) {
        userVariableForm.getTab().markChanges(changed);
    }
}
