package org.pacos.common.view.grid;

import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GridMultiSelectExtensionTest {

    private Grid<String> grid;
    private ListDataProvider<String> dataProvider;

    @BeforeEach
    void setUp() {
        dataProvider = new ListDataProvider<>(List.of("Item1", "Item2", "Item3", "Item4", "Item5"));
        grid = new Grid<>();
        grid.setDataProvider(dataProvider);
        GridMultiSelectExtension.extend(grid);
    }

    @Test
    void testExtendWithUnsupportedDataProvider() {
        var unsupportedGrid = Mockito.mock(Grid.class);
        when(unsupportedGrid.getDataProvider()).thenReturn(mock(HierarchicalDataProvider.class));
        assertThrows(UnsupportedOperationException.class, () -> GridMultiSelectExtension.extend(unsupportedGrid));
    }

    @Test
    void testCtrlKeyMultiSelect() {
        ItemClickEvent<String> clickEvent1 = mock(ItemClickEvent.class);
        when(clickEvent1.getItem()).thenReturn("Item1");
        when(clickEvent1.isCtrlKey()).thenReturn(true);

        ItemClickEvent<String> clickEvent2 = mock(ItemClickEvent.class);
        when(clickEvent2.getItem()).thenReturn("Item3");
        when(clickEvent2.isCtrlKey()).thenReturn(true);

        GridMultiSelectExtension.onItemClickEvent(grid, clickEvent1);
        GridMultiSelectExtension.onItemClickEvent(grid, clickEvent2);

        assertTrue(grid.getSelectedItems().containsAll(List.of("Item1", "Item3")));
    }

    @Test
    void testShiftKeyMultiSelect() {
        grid.select("Item1"); // Simulate initial selection

        ItemClickEvent<String> clickEvent = mock(ItemClickEvent.class);
        when(clickEvent.getItem()).thenReturn("Item4");
        when(clickEvent.isShiftKey()).thenReturn(true);

        GridMultiSelectExtension.onItemClickEvent(grid, clickEvent);

        List<String> expectedSelection = List.of("Item1", "Item2", "Item3", "Item4");
        assertEquals(expectedSelection.size(), grid.getSelectedItems().size());
        assertTrue(grid.getSelectedItems().containsAll(expectedSelection));
    }

    @Test
    void testRegularClickSelect() {
        ItemClickEvent<String> clickEvent = mock(ItemClickEvent.class);
        when(clickEvent.getItem()).thenReturn("Item2");

        GridMultiSelectExtension.onItemClickEvent(grid, clickEvent);

        assertEquals(1, grid.getSelectedItems().size());
        assertTrue(grid.getSelectedItems().contains("Item2"));
    }

    @Test
    void testDeselectWithCtrlClick() {
        grid.select("Item2");

        ItemClickEvent<String> clickEvent = mock(ItemClickEvent.class);
        when(clickEvent.getItem()).thenReturn("Item2");
        when(clickEvent.isCtrlKey()).thenReturn(true);

        GridMultiSelectExtension.onItemClickEvent(grid, clickEvent);

        assertTrue(grid.getSelectedItems().isEmpty());
    }

    @Test
    void testGetLastSelectedItem() {
        grid.select("Item3");
        String lastSelectedItem = GridMultiSelectExtension.getLastSelectedItem(grid);

        assertNotNull(lastSelectedItem);
        assertEquals("Item3", lastSelectedItem);
    }

    @Test
    void testGetLastSelectedItemWhenNoSelection() {
        String lastSelectedItem = GridMultiSelectExtension.getLastSelectedItem(grid);

        assertNull(lastSelectedItem);
    }
}
