package org.pacos.core.component.variable.view.plugin;

import org.pacos.core.component.variable.event.user.UpdateUserVariableEvent;
import org.pacos.core.component.variable.proxy.UserVariableCollectionProxy;
import org.pacos.core.component.variable.proxy.UserVariableProxy;
import org.pacos.core.component.variable.view.grid.RemoveEvent;
import org.pacos.core.component.variable.view.grid.SaveEvent;
import org.pacos.core.component.variable.view.grid.VariableGrid;

public class PluginVariableGrid extends VariableGrid {

    private final transient UserVariableProxy userVariableProxy;
    private final transient UserVariableCollectionProxy userVariableCollectionProxy;

    public PluginVariableGrid(UserVariableProxy userVariableProxy,
                              UserVariableCollectionProxy userVariableCollectionProxy) {
        super();
        this.userVariableProxy = userVariableProxy;
        this.userVariableCollectionProxy = userVariableCollectionProxy;
        this.configurator()
                .withCheckBoxColumn()
                .withNameColumn(false)
                .withInitValueColumn(false)
                .withCurrentValueColumn(true);
        this.enableEditor(false);
    }

    @Override
    public RemoveEvent onRemoveEvent() {
        return null;
    }

    @Override
    public SaveEvent onSaveEvent() {
        return e -> {
            UpdateUserVariableEvent.fireEvent(e, userVariableProxy, userVariableCollectionProxy);
            getDataCommunicator().refresh(e);
        };
    }

    @Override
    public void onElemStateChange() {
        //not monitored by tab change monitor
    }
}


