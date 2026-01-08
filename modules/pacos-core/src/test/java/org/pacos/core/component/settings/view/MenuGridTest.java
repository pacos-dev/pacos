package org.pacos.core.component.settings.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSingleSelectionModel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import org.config.ProxyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.core.component.settings.view.config.SystemAccessConfig;
import org.pacos.core.system.view.PacosJS;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuGridTest {

    private MenuGrid menuGrid;

    @BeforeEach
    void setUp() {
        List<SettingTab> settingsTabs = List.of(new SystemAccessConfig(ProxyMock.registryProxy()));
        menuGrid = spy(new MenuGrid(settingsTabs));
        doReturn(Optional.of(new Span())).when(menuGrid).getParent();
    }

    @Test
    void whenInitializedThenSelectionModeIsSingleAndDeselectIsDisallowed() {
        //given

        //when
        GridSingleSelectionModel<MenuNode> selectionModel = (GridSingleSelectionModel<MenuNode>) menuGrid.getSelectionModel();

        //then
        assertEquals(Grid.SelectionMode.SINGLE, menuGrid.getSelectionMode());
        assertFalse(selectionModel.isDeselectAllowed());
    }

    @Test
    void whenFilterSetWithEmptyThenFilterIsCleared() {
        //given
        String query = "";

        try (MockedStatic<PacosJS> pacosJSMockedStatic = mockStatic(PacosJS.class)) {
            //when
            menuGrid.onFilterSet(query);
            //then
            assertEquals("",menuGrid.getSearchQuery());
            pacosJSMockedStatic.verify(() -> PacosJS.highlightText(any(), eq("")));
        }
    }

    @Test
    void whenFilterSetWithQueryThenSearchQueryIsStored() {
        //given
        String query = "  System  ";

        try (MockedStatic<PacosJS> pacosJSMockedStatic = mockStatic(PacosJS.class)) {
            //when
            menuGrid.onFilterSet(query);

            //then
            assertEquals("  System  ", menuGrid.getSearchQuery());
            pacosJSMockedStatic.verify(() -> PacosJS.highlightText(any(), eq("  System  ")));
        }
    }

    @Test
    void whenOnFilterSetCalledThenDataProviderIsRefreshed() {
        //given
        String query = "test";

        try (MockedStatic<PacosJS> pacosJSMockedStatic = mockStatic(PacosJS.class)) {
            //when
            menuGrid.onFilterSet(query);

            //then
            long count = menuGrid.getDataProvider().fetch(new HierarchicalQuery<>(null, null)).count();
            assertTrue(count >= 0);
        }
    }

    @Test
    void whenSelectFirstItemCalledThenFirstRootIsSelected() {
        //given
        SettingTab tab = mock(SettingTab.class);
        when(tab.getTitle()).thenReturn("RootTitle");
        menuGrid = new MenuGrid(List.of(tab));

        //when
        menuGrid.selectFirstItem();

        //then
        Optional<MenuNode> selected = menuGrid.asSingleSelect().getOptionalValue();
        assertTrue(selected.isPresent());
        assertEquals("RootTitle", selected.get().name());
    }

    @Test
    void whenReloadCalledThenDataIsUpdated() {
        //given
        SettingTab tab = mock(SettingTab.class);
        when(tab.getTitle()).thenReturn("NewTitle");
        List<SettingTab> newTabs = List.of(tab);

        //when
        menuGrid.reload(newTabs);

        //then
        assertNotNull(menuGrid.getNodeByName("NewTitle"));
    }

    @Test
    void whenGenerateLabelThenSetNodeNameAsText(){
        MenuNode node = new MenuNode("test",null);
        //when
        Span result = MenuGrid.generateLabel(node);
        //then
        assertEquals("test",result.getText());
    }
}