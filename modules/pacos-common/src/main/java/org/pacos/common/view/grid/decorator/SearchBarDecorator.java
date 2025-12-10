package org.pacos.common.view.grid.decorator;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.pacos.base.utils.component.TextFieldUtils;
import org.pacos.common.event.ColumnFilterEvent;

/**
 * Add header row with filter fields
 */
public final class SearchBarDecorator {
    private SearchBarDecorator() {
    }

    public static TextFieldUtils configureFor(Grid<?> grid, Grid.Column<?> column, ColumnFilterEvent filterEvent) {
        TextFieldUtils searchField = TextFieldUtils.configureSearchField("Filter..");
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        HeaderRow headerRow = grid.appendHeaderRow();
        HeaderRow.HeaderCell cell = headerRow.getCell(column);
        cell.setComponent(searchField);

        searchField.addValueChangeListener(e -> filterEvent.onFilterSet(e.getValue()));
        return searchField;
    }
}
