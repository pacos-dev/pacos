package org.pacos.core.component.variable.system.global;

import org.pacos.base.event.SystemEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.variable.proxy.SystemVariableProxy;
import org.pacos.core.component.variable.view.global.SystemVariableGrid;

/**
 * Internal implementation of the variable module system
 */
public class GlobalVariableSystem extends SystemEvent<GlobalVariableEvent> {

    private final SystemVariableProxy systemVariableProxy;
    private final UISystem uiSystem;
    private SystemVariableGrid variableGrid;

    public GlobalVariableSystem(SystemVariableProxy globalVariableProxy) {
        this.uiSystem = UserSession.getCurrent().getUISystem();
        this.systemVariableProxy = globalVariableProxy;
    }

    public void setVariableGrid(SystemVariableGrid variableGrid) {
        this.variableGrid = variableGrid;
    }

    public SystemVariableProxy getSystemVariableProxy() {
        return systemVariableProxy;
    }

    public UISystem getUiSystem() {
        return uiSystem;
    }

    public SystemVariableGrid getVariableGrid() {
        return variableGrid;
    }
}
