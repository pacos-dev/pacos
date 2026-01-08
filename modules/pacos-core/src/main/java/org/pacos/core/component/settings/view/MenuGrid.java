package org.pacos.core.component.settings.view;

import com.vaadin.flow.component.grid.GridSingleSelectionModel;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializablePredicate;
import org.pacos.base.component.Style;
import org.pacos.base.component.Theme;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.common.event.ColumnFilterEvent;
import org.pacos.common.view.grid.decorator.SearchBarDecorator;
import org.pacos.core.system.view.PacosJS;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class MenuGrid extends TreeGrid<MenuNode> implements ColumnFilterEvent {

    private final MenuData menuData;
    private final HierarchicalConfigurableFilterDataProvider<MenuNode, Void, String> filterProvider;
    private String searchQuery;

    public MenuGrid(List<SettingTab> settingsTab) {
        this.menuData = new MenuData(settingsTab);
        configureStyle();

        var column = addComponentHierarchyColumn(MenuGrid::generateLabel).setSortable(false);

        SearchBarDecorator.configureFor(this, column, this);
        SerializableBiFunction<Void, String, SerializablePredicate<MenuNode>> filterCombiner =
                (ignored, query) -> (MenuNode node) -> {
                    if (query == null || query.isBlank()) {
                        return true;
                    }
                    if(node.entity()==null || node.entity().getSearchIndex()==null){
                        return false;
                    }
                    return menuData.isVisible(node, query.toLowerCase().trim());
                };
        TreeDataProvider<MenuNode> baseProvider = new TreeDataProvider<>(menuData);
        this.filterProvider
                = baseProvider.withConfigurableFilter(filterCombiner);
        setDataProvider(filterProvider);
    }

    static Span generateLabel(MenuNode node) {
        Span title = new Span(node.name());
        title.getElement().setAttribute("data-node-id", UUID.randomUUID().toString());
        return title;
    }

    private void configureStyle() {
        addThemeName(Theme.NO_MARGIN.getName());
        addThemeName(Theme.NO_PADDING.getName());
        addThemeName(Theme.NO_BORDER.getName());
        addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
        setSelectionMode(SelectionMode.SINGLE);
        GridSingleSelectionModel<MenuNode> selectionModel =
                (GridSingleSelectionModel<MenuNode>) getSelectionModel();
        selectionModel.setDeselectAllowed(false);
        getStyle().set(Style.USER_SELECT.value(), "none");
    }

    private void selectFirstFound(String query) {
        MenuNode selected = asSingleSelect().getValue();
        if (selected != null && MenuData.isVisibleNode(selected, query)) {
            return;
        }
        Optional<MenuNode> firstItem = getDataProvider()
                .fetch(new HierarchicalQuery<>(null, null))
                .findFirst();
        if (firstItem.isPresent()) {
            MenuNode item = firstItem.get();
            if (MenuData.isVisibleNode(item, query)) {
                select(item);
            } else {
                getDataCommunicator().expand(item);
                Optional<MenuNode> found = menuData.getChildren(item).stream().filter(node -> menuData.isVisible(node, query))
                        .findFirst();
                found.ifPresent(this::select);
            }
        } else {
            select(null);
        }

    }

    void selectFirstItem() {
        select(menuData.getRootItems().getFirst());
    }

    @Override
    public void onFilterSet(String query) {
        this.searchQuery = query;
        if (query == null || query.isBlank()) {
            filterProvider.setFilter(null);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            filterProvider.setFilter(lowerQuery);
        }
        getDataCommunicator().reset();
        getDataProvider().refreshAll();
        selectFirstFound(query);
        this.getParent().ifPresent(parent->
                PacosJS.highlightText(parent, searchQuery));

    }

    String getSearchQuery() {
        return searchQuery;
    }

    void reload(List<SettingTab> settingTabs) {
        menuData.reload(settingTabs);
    }

    MenuNode getNodeByName(String node) {
        return menuData.getNodeByName(node);
    }

    Set<String> itemNames() {
        return menuData.itemNames();
    }
}
