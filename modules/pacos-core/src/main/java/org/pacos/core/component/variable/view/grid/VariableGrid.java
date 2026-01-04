package org.pacos.core.component.variable.view.grid;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.data.provider.ListDataProvider;
import org.pacos.base.component.Theme;
import org.pacos.common.view.grid.GridMultiSelectExtension;
import org.pacos.core.component.variable.dto.UserVariableDTO;

public abstract class VariableGrid extends Grid<UserVariableDTO> {

    private final transient VariableGridColumnConfigurator configurator;

    protected transient List<UserVariableDTO> variables = new ArrayList<>();
    private UserVariableDTO last = new UserVariableDTO();
    private final Editor<UserVariableDTO> editor;
    private boolean dynamicLineEnabled;

    protected VariableGrid() {
        super(UserVariableDTO.class, false);
        this.editor = getEditor();

        addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        getThemeNames().add(Theme.SMALL.getName());
        addClassName("no-select");
        setSizeFull();
        setAllRowsVisible(true);
        setMultiSort(true);

        GridMultiSelectExtension.extend(this);
        setPartNameGenerator(item -> {
            if (item.equals(last) && !getEditor().isOpen()) {
                return "last";
            }
            return null;
        });

        this.configurator = new VariableGridColumnConfigurator(this);

    }


    public VariableGridColumnConfigurator configurator() {
        return configurator;
    }

    public void enableEditor(boolean dynamicLineEnabled) {

        this.dynamicLineEnabled = dynamicLineEnabled;
        if (dynamicLineEnabled) {
            addThemeName(Theme.PARAM_GRID.getName());
        }
        VariableGridEditor.createEditor(this, configurator, editor);
        this.editor.addCloseListener(e -> {
            onElemStateChange();
            if (editor.getBinder().isValid()) {
                onSaveEvent().onSave(e.getItem());
            }
        });
    }

    public abstract RemoveEvent onRemoveEvent();

    public abstract SaveEvent onSaveEvent();

    public abstract void onElemStateChange();

    public void updateVariableList(List<UserVariableDTO> variables) {
        setVariables(new ArrayList<>(variables));
        setEnabled(true);
    }

    public void removeParam(UserVariableDTO param) {
        variables.remove(param);
        getDataProvider().refreshAll();
        onRemoveEvent().onRemove(param);
    }

    public List<UserVariableDTO> getVariables() {
        return variables.stream().filter(k -> !k.isEmpty()).toList();
    }

    public boolean isLast(UserVariableDTO param) {
        return last == param;
    }

    public void setVariables(List<UserVariableDTO> variables) {

        if (variables == null) {
            setVariablesGrid(new ArrayList<>());
            setEnabled(false);
        } else {
            setVariablesGrid(new ArrayList<>(variables));
            setEnabled(true);
        }
    }

    protected void editorCloseListener() {
        if (dynamicLineEnabled && !last.isEmpty()) {
            getDataCommunicator().refresh(last);
            last = new UserVariableDTO();
            last.setEnabled(true);
            variables.add(last);
            onElemStateChange();
        }
        getDataProvider().refreshAll();
    }

    private void setVariablesGrid(List<UserVariableDTO> items) {
        this.variables = items;
        if (dynamicLineEnabled) {
            last.setEnabled(true);
            items.add(last);
        }
        setDataProvider(new ListDataProvider<>(items) {
            @Override
            public Object getId(UserVariableDTO item) {
                return item.getUuid().toString();
            }
        });
        getDataProvider().refreshAll();
    }

    public void updateVariable(UserVariableDTO variable) {
        variables.stream().filter(v -> v.getId().equals(variable.getId())).forEach(v -> v.setEnabled(variable.isEnabled()));
        getDataCommunicator().refresh(variable);
    }
}
