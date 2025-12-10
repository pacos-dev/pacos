package org.pacos.common.view.grid;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.pacos.base.utils.component.TextFieldUtils;
import org.pacos.common.data.HasName;

public class GridFilterNameExtension<T extends HasName> {

    public GridFilterNameExtension(Grid<T> grid, Grid.Column<T> column) {
        HeaderRow headerRow = grid.appendHeaderRow();
        TextFieldUtils searchField = TextFieldUtils.configureSearchField("Filter..");
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        HeaderRow.HeaderCell cell = headerRow.getCell(column);
        cell.setComponent(searchField);

        searchField.addValueChangeListener(e -> grid.getListDataView().refreshAll());

        grid.getListDataView().addFilter(e -> {
            String searchTerm = searchField.getValue().trim();

            if (searchTerm.isEmpty())
                return true;

            return matches(e.getDisplayName(), searchTerm);
        });
    }

    private boolean matches(String value, String searchTerm) {
        return searchTerm == null || searchTerm.isEmpty()
                || value.toLowerCase().contains(searchTerm.toLowerCase());
    }


}
