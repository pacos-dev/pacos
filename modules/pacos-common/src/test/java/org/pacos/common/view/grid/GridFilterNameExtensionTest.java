package org.pacos.common.view.grid;

import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.utils.component.TextFieldUtils;
import org.pacos.common.data.HasName;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GridFilterNameExtensionTest {

    private Grid<NamedItem> grid;
    private TextFieldUtils searchField;

    @BeforeEach
    void setUp() {
        grid = new Grid<>();
        Grid.Column<NamedItem> column = grid.addColumn(NamedItem::getDisplayName);
        grid.setItems(List.of(new NamedItem("Alpha"), new NamedItem("Beta"), new NamedItem("alphabet")));
        new GridFilterNameExtension<>(grid, column);
        searchField = (TextFieldUtils) grid.getHeaderRows().get(1).getCell(column).getComponent();
    }

    @Test
    void whenFilterIsEmptyThenReturnAllItems() {
        assertEquals(List.of("Alpha", "Beta", "alphabet"), filteredNames());
    }

    @Test
    void whenFilterContainsCaseInsensitiveTermThenReturnMatchingItems() {
        searchField.setValue(" ALP ");

        assertEquals(List.of("Alpha", "alphabet"), filteredNames());
    }

    @Test
    void whenFilterDoesNotMatchAnyItemThenReturnNoItems() {
        searchField.setValue("missing");

        assertEquals(List.of(), filteredNames());
    }

    private List<String> filteredNames() {
        return grid.getListDataView().getItems()
                .map(NamedItem::getDisplayName)
                .toList();
    }

    private record NamedItem(String displayName) implements HasName {
        @Override
        public String getDisplayName() {
            return displayName;
        }
    }
}
