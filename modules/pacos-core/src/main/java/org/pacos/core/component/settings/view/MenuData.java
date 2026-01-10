package org.pacos.core.component.settings.view;

import com.vaadin.flow.data.provider.hierarchy.TreeData;
import org.pacos.base.component.setting.SettingTab;

import java.util.*;

public class MenuData extends TreeData<MenuNode> {

    private final Map<String, MenuNode> nodesMap = new HashMap<>();

    public MenuData(List<SettingTab> settingsTab) {
        loadAll(settingsTab);
    }

    private void loadAll(List<SettingTab> settingsTab) {
        Comparator<MenuNode> comparator = Comparator
                .comparingInt(MenuNode::getOrder)
                .thenComparing(MenuNode::name);

        List<MenuNode> roots = settingsTab.stream().filter(s -> !isGroupDefined(s))
                .map(st -> new MenuNode(st.getTitle(), st))
                .sorted(comparator)
                .toList();
        roots.forEach(node -> nodesMap.put(node.name(), node));
        addRootItems(roots);
        settingsTab.stream().filter(MenuData::isGroupDefined).forEach(st -> {
            String rootGroup = st.getGroup()[0];
            //Add rootItem if needed
            if (!nodesMap.containsKey(rootGroup)) {
                MenuNode menuNode = new MenuNode(rootGroup, null);
                nodesMap.put(rootGroup, menuNode);
                addRootItems(menuNode);
            }
            //Create tree
            for (int i = 1; i < st.getGroup().length; i++) {
                String group = st.getGroup()[i];
                MenuNode parent = nodesMap.get(group);
                MenuNode menuNode = new MenuNode(group, null);
                if (!nodesMap.containsKey(group)) {
                    nodesMap.put(group, menuNode);
                    addItem(parent, menuNode);
                }
            }

            String lastGroup = st.getGroup()[st.getGroup().length - 1];
            MenuNode node = new MenuNode(st.getTitle(), st);
            nodesMap.put(node.name(), node);
            addItem(nodesMap.get(lastGroup), node);
        });
    }

    private static boolean isGroupDefined(SettingTab s) {
        return s.getGroup() != null && s.getGroup().length > 0;
    }

    public void reload(List<SettingTab> settingTabs) {
        new ArrayList<>(getRootItems()).forEach(this::removeItem);
        loadAll(settingTabs);
    }

    public Set<String> itemNames() {
        return nodesMap.keySet();
    }

    MenuNode getNodeByName(String node) {
        return nodesMap.get(node);
    }

    boolean isVisible(MenuNode node, String query) {
        if (isVisibleNode(node, query)) return true;
        return getChildren(node).stream().anyMatch(child -> isVisible(child, query));
    }

    static boolean isVisibleNode(MenuNode node, String query) {
        boolean matches = node.name()!=null && node.name().toLowerCase().contains(query);
        if (matches) return true;

        if(node.entity()!=null) {
            String searchIndex = node.entity().getSearchIndex();
            if (searchIndex != null) {
                searchIndex = searchIndex.toLowerCase();
                matches = searchIndex.contains(query);
            }
        }
        return matches;
    }
}
