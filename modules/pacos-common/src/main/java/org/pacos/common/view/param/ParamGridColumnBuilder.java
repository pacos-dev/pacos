package org.pacos.common.view.param;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;

/**
 * @deprecated
 */
@Deprecated
public class ParamGridColumnBuilder {

    private final ParamGrid paramGrid;
    private Grid.Column<Param> nameColumn;
    private Grid.Column<Param> valueColumn;

    ParamGridColumnBuilder(ParamGrid paramGrid) {
        this.paramGrid = paramGrid;
    }

    void withNameColumn(String header) {
        this.nameColumn = paramGrid.addColumn(Param::getName).setResizable(true).setHeader(header);

    }

    void withValueColumn(String header) {
        this.valueColumn = paramGrid.addColumn(Param::getValue).setResizable(true).setHeader(header);

    }

    public void withButtonColumn() {
        paramGrid.addColumn(new ComponentRenderer<>(Button::new, (button, param) -> {
            if (paramGrid.isLast(param)) {
                button.setVisible(false);
            }
            button.addThemeVariants(ButtonVariant.LUMO_ICON,
                    ButtonVariant.LUMO_TERTIARY);
            button.addClassName("grid");
            button.addClickListener(e -> paramGrid.removeParam(param));
            button.setIcon(new Icon(VaadinIcon.TRASH));
        })).setWidth("20px").setHeader("");

    }

    public Grid.Column<Param> getNameColumn() {
        return nameColumn;
    }

    public Grid.Column<Param> getValueColumn() {
        return valueColumn;
    }
}
