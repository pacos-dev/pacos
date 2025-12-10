package org.pacos.core.component.variable.view.grid;

import org.pacos.core.component.variable.dto.UserVariableDTO;

@FunctionalInterface
public interface RemoveEvent {
    void onRemove(UserVariableDTO line);
}
