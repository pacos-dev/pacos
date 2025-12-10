package org.pacos.core.component.variable.view.global;

import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import org.pacos.base.event.UISystem;
import org.pacos.base.variable.ScopeDefinition;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.dto.mapper.SystemVariableToVariableMapper;
import org.pacos.core.component.variable.event.global.EditVariableItemEvent;
import org.pacos.core.component.variable.system.global.GlobalVariableEvent;
import org.pacos.core.component.variable.system.global.GlobalVariableSystem;
import org.vaadin.addons.variablefield.data.Scope;

public class SystemVariableGrid extends Grid<SystemVariableDTO> {

    private final transient List<SystemVariableDTO> items;
    private static final Scope SYSTEM_SCOPE = ScopeDefinition.SYSTEM;

    public SystemVariableGrid(GlobalVariableSystem system) {
        super(SystemVariableDTO.class, false);
        system.setVariableGrid(this);
        addColumn(SystemVariableDTO::getName);
        this.items = system.getSystemVariableProxy().loadVariables();
        setHeightFull();
        setItems(items);
        addItemDoubleClickListener(e -> EditVariableItemEvent.fireEvent(e.getItem(), system));

        system.subscribe(GlobalVariableEvent.REFRESH_ENTRY, e -> refreshEntry((SystemVariableDTO) e));
        system.subscribe(GlobalVariableEvent.ADD_NEW_VARIABLE_TO_GRID, e -> addItem((SystemVariableDTO) e));
        system.subscribe(GlobalVariableEvent.REMOVED_ENTRY, e -> removeEntry((SystemVariableDTO) e));
    }

    private void removeEntry(SystemVariableDTO e) {
        items.remove(e);
        setItems(items);

        UISystem.getCurrent().getVariableManager().removeVariable(SYSTEM_SCOPE, SystemVariableToVariableMapper.map(e));
    }

    private void addItem(SystemVariableDTO e) {
        items.add(e);
        setItems(items);

        UISystem.getCurrent().getVariableManager().updateVariable(SYSTEM_SCOPE, SystemVariableToVariableMapper.map(e));
    }

    private void refreshEntry(SystemVariableDTO e) {
        setItems(items);

        UISystem.getCurrent().getVariableManager().updateVariable(SYSTEM_SCOPE, SystemVariableToVariableMapper.map(e));
    }
}


