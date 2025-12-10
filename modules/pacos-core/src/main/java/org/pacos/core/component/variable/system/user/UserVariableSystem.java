package org.pacos.core.component.variable.system.user;

import org.pacos.base.event.SystemEvent;
import org.pacos.base.event.UISystem;
import org.pacos.core.component.variable.proxy.UserVariableCollectionProxy;
import org.pacos.core.component.variable.proxy.UserVariableProxy;
import org.pacos.core.component.variable.view.PanelVariable;
import org.pacos.core.component.variable.view.user.UserVariableCollectionGrid;
import org.pacos.core.component.variable.view.user.UserVariableTabSheet;

/**
 * Internal implementation of the variable module system
 */
public class UserVariableSystem extends SystemEvent<UserVariableEvent> {

    private final PanelVariable panel;
    private final UserVariableCollectionProxy variableCollectionProxy;
    private final UISystem uiSystem;
    private final UserVariableProxy userVariableProxy;
    private UserVariableCollectionGrid collectionGrid;
    private UserVariableTabSheet userVariableTabSheet;

    public UserVariableSystem(PanelVariable panel, UISystem uiSystem, UserVariableCollectionProxy
            userVariableCollectionProxy, UserVariableProxy userVariableProxy) {
        this.panel = panel;
        this.uiSystem = uiSystem;
        this.variableCollectionProxy = userVariableCollectionProxy;
        this.userVariableProxy = userVariableProxy;
    }


    public void setCollectionGrid(UserVariableCollectionGrid collectionGrid) {
        this.collectionGrid = collectionGrid;
    }

    public void setVariableGrid(UserVariableTabSheet userVariableTabSheet) {
        this.userVariableTabSheet = userVariableTabSheet;
    }

    public PanelVariable getPanel() {
        return panel;
    }

    public UserVariableCollectionProxy getUserVariableCollectionProxy() {
        return variableCollectionProxy;
    }

    public UISystem getUiSystem() {
        return uiSystem;
    }

    public UserVariableProxy getUserVariableProxy() {
        return userVariableProxy;
    }

    public UserVariableCollectionGrid getCollectionGrid() {
        return collectionGrid;
    }

    public UserVariableTabSheet getUserVariableTabSheet() {
        return userVariableTabSheet;
    }
}
