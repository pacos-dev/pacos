package org.pacos.common.view.grid;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.data.provider.ListDataProvider;

public final class GridMultiSelectExtension {

    private GridMultiSelectExtension() {

    }

    public static <T> void extend(Grid<T> grid) {
        if (!(grid.getDataProvider() instanceof ListDataProvider)) {
            throw new UnsupportedOperationException("Grid data provider not supported");
        }

        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addAttachListener(e -> grid.getElement()
                .executeJs("this.getElementsByTagName(\"vaadin-grid-flow-selection-column\")[0].hidden = true;"));
        grid.addItemClickListener(event -> onItemClickEvent(grid, event));
    }

    static <T> void onItemClickEvent(Grid<T> grid, ItemClickEvent<T> event) {
        var item = event.getItem();
        if (item == null) {
            return;
        }
        if (event.isCtrlKey()) {
            appendModeSelect(grid, item);
        } else if (event.isShiftKey()) {
            multiModeSelect(grid, item);
        } else {
            singleSelectMode(grid, item);
        }
    }

    private static <T> void singleSelectMode(Grid<T> grid, T item) {
        grid.deselectAll();
        grid.select(item);
    }

    private static <T> void multiModeSelect(Grid<T> grid, T item) {
        var lastSelectedItem = getLastSelectedItem(grid);
        if (lastSelectedItem != null) {
            Collection<T> items = ((ListDataProvider<T>) grid.getDataProvider()).getItems();
            if (items instanceof List<T> list) {
                var startIndex = list.indexOf(lastSelectedItem);
                var endIndex = list.indexOf(item);

                int fromIndex = Math.min(startIndex, endIndex);
                int toIndex = Math.max(startIndex, endIndex);

                for (int i = fromIndex; i <= toIndex; i++) {
                    grid.select(list.get(i));
                }
            }
        }
    }

    private static <T> void appendModeSelect(Grid<T> grid, T item) {
        if (grid.getSelectedItems().contains(item)) {
            grid.deselect(item);
        } else {
            grid.select(item);
        }
    }

    static <T> T getLastSelectedItem(Grid<T> grid) {
        Set<T> selectedItems = grid.getSelectedItems();
        if (!selectedItems.isEmpty()) {
            return selectedItems.iterator().next();
        }
        return null;
    }
}
