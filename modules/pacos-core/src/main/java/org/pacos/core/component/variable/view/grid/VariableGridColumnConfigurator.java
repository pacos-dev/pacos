package org.pacos.core.component.variable.view.grid;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import lombok.Getter;
import org.pacos.core.component.variable.dto.UserVariableDTO;

public class VariableGridColumnConfigurator {

    private final VariableGrid variableGrid;
    @Getter
    private Grid.Column<UserVariableDTO> nameColumn;
    @Getter
    private boolean nameColumnEditable;
    @Getter
    private Grid.Column<UserVariableDTO> initValueColumn;
    @Getter
    private boolean initValueColumnEditable;
    @Getter
    private Grid.Column<UserVariableDTO> currentValueColumn;
    @Getter
    private boolean currentValueColumnEditable;

    public VariableGridColumnConfigurator(VariableGrid variableGrid) {
        this.variableGrid = variableGrid;
    }


    public VariableGridColumnConfigurator withNameColumn(boolean editable) {
        this.nameColumn = variableGrid.addColumn(UserVariableDTO::getName)
                .setResizable(true).setSortable(true).setHeader("Name");
        this.nameColumnEditable = editable;
        return this;
    }

    public VariableGridColumnConfigurator withInitValueColumn(boolean editable) {
        this.initValueColumn = variableGrid.addColumn(UserVariableDTO::getInitialValue)
                .setResizable(true).setSortable(true).setHeader("Initial value");
        this.initValueColumnEditable = editable;
        return this;

    }

    public VariableGridColumnConfigurator withCurrentValueColumn(boolean editable) {
        this.currentValueColumn = variableGrid.addColumn(UserVariableDTO::getCurrentValue)
                .setResizable(true).setSortable(true).setHeader("Current value");
        this.currentValueColumnEditable = editable;
        return this;

    }

    public VariableGridColumnConfigurator withCheckBoxColumn() {
        variableGrid.addColumn(new ComponentRenderer<>(Checkbox::new, (checkbox, param) -> {
            if (variableGrid.isLast(param)) {
                checkbox.setVisible(false);
            }
            checkbox.setValue(param.isEnabled());
            checkbox.addValueChangeListener(e ->
                    {
                        param.setEnabled(e.getValue());
                        variableGrid.onSaveEvent().onSave(param);
                    }
            );
        })).setSortable(true).setWidth("30px").setHeader("Enabled");
        return this;
    }

    public VariableGridColumnConfigurator withButtonColumn() {
        variableGrid.addColumn(new ComponentRenderer<>(Button::new, (button, param) -> {
            if (variableGrid.isLast(param)) {
                button.setVisible(false);
            }
            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                    ButtonVariant.LUMO_TERTIARY);
            button.addClassName("grid");
            button.addClickListener(e -> variableGrid.removeParam(param));
            button.setIcon(new Icon(VaadinIcon.TRASH));
        })).setWidth("20px").setHeader("");
        return this;
    }

}
