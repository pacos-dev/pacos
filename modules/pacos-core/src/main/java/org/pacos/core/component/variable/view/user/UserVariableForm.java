package org.pacos.core.component.variable.view.user;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.pacos.common.view.grid.decorator.BlurAndFocusDecorator;
import org.pacos.common.view.tab.TabMonitoring;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.component.variable.event.user.FormChangedEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;
import org.pacos.core.component.variable.view.grid.RemoveEvent;
import org.pacos.core.component.variable.view.grid.SaveEvent;
import org.pacos.core.component.variable.view.grid.VariableGrid;

public class UserVariableForm extends VerticalLayout {
    private final VariableGrid variableGrid;
    private final TabMonitoring tab;
    private final UserVariableCollectionDTO variableCollection;
    private int collectionHash;

    public UserVariableForm(UserVariableSystem system, UserVariableCollectionDTO variableCollection, TabMonitoring tab) {
        this.tab = tab;
        this.variableGrid = new VariableGrid() {
            @Override
            public RemoveEvent onRemoveEvent() {
                markAsChanged();
                return e -> {
                };
            }

            @Override
            public SaveEvent onSaveEvent() {
                markAsChanged();
                return e -> {
                    if (e.getId() == null && e.getCurrentValue().isEmpty()) {
                        e.setCurrentValue(e.getInitialValue());
                    }
                    getDataCommunicator().refresh(e);
                };
            }

            @Override
            public void onElemStateChange() {
                markAsChanged();
            }
        };
        this.variableGrid.configurator()
                .withCheckBoxColumn()
                .withNameColumn(true)
                .withInitValueColumn(true)
                .withCurrentValueColumn(true)
                .withButtonColumn();
        this.variableGrid.enableEditor(true);
        this.variableCollection = variableCollection;
        setVariables(system.getUserVariableProxy().loadVariables(variableCollection.getId()));
        add(variableGrid);

        BlurAndFocusDecorator.configureFor(variableGrid);
    }

    public List<UserVariableDTO> getVariables() {
        return this.variableGrid.getVariables();
    }

    /**
     * Set stored/saved collection. This will also replace current colection hash,
     * so no changes will be detected
     */
    public void setVariables(List<UserVariableDTO> collections) {
        this.collectionHash = calculateCollectionHashCode(collections);
        this.variableGrid.setVariables(collections);
    }

    public TabMonitoring getTab() {
        return tab;
    }

    /**
     * Replace variable lists and calculate changes
     */
    public void modifyVariables(List<UserVariableDTO> variables) {
        variables.forEach(v -> v.setCollectionId(variableCollection.getId()));
        this.variableGrid.setVariables(variables);
        markAsChanged();
    }

    /**
     * Calculate new hash on variable list and check if list was changed
     */
    private void markAsChanged() {
        int newHash = calculateCollectionHashCode(variableGrid.getVariables());
        boolean changed = newHash != collectionHash;
        FormChangedEvent.fireEvent(changed, this);
    }

    private int calculateCollectionHashCode(List<UserVariableDTO> variables) {
        return variables.stream().mapToInt(this::hashCodeUserVariableDTO).sum();
    }

    /**
     * Calculate unique hash for given variable. UUID is used to ensure that result will be unique for each row
     */
    private int hashCodeUserVariableDTO(UserVariableDTO line) {
        return Objects.hash(line.getId() + "" + line.isEnabled() + line.getName() + line.getCurrentValue() + line.getInitialValue() + line.getUuid());
    }

    /**
     * Called when user made a change in plugin in the same time when panel is opened
     */
    public void refreshVariableChangesFromPlugin(UserVariableDTO variable) {
        if (Objects.equals(variable.getCollectionId(), variableCollection.getId())) {
            markAsChanged();
            this.variableGrid.getDataCommunicator().refresh(variable);
            this.variableGrid.updateVariable(variable);
        }
    }

    public Set<UserVariableDTO> getSelectedItems() {
        return variableGrid.getSelectedItems();
    }

    public void removeParam(UserVariableDTO userVariableDTO) {
        variableGrid.removeParam(userVariableDTO);
    }

    public boolean isEditorClosed() {
        return !variableGrid.getEditor().isOpen();
    }

    public VariableGrid getVariableGrid() {
        return variableGrid;
    }
}
