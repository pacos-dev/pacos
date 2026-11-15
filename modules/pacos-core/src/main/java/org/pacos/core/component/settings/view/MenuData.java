package org.pacos.core.component.settings.view;

import com.vaadin.flow.data.provider.hierarchy.TreeData;
import org.pacos.base.component.setting.SettingTab;

import java.util.*;

public class MenuData extends TreeData<MenuNode> {

    private final Map<List<String>, MenuNode> groupNodes = new HashMap<>();
    private final Map<String, MenuNode> rootNodes = new HashMap<>();
    private final Map<String, MenuNode> tabNodes = new HashMap<>();

    public MenuData(List<SettingTab> settingsTab) {
        loadAll(settingsTab);
    }

    private void loadAll(List<SettingTab> settingsTab) {
        Comparator<MenuNode> comparator = Comparator
                .comparingInt(MenuNode::getOrder)
                .thenComparing(MenuNode::name);

        List<MenuNode> roots = settingsTab.stream()
                .filter(settingTab -> !isGroupDefined(settingTab))
                .map(settingTab -> new MenuNode(settingTab.getTitle(), settingTab))
                .sorted(comparator)
                .toList();

        roots.forEach(node -> {
            if (tabNodes.putIfAbsent(node.name(), node) == null) {
                rootNodes.put(node.name(), node);
                addRootItems(node);
            }
        });

        settingsTab.stream()
                .filter(MenuData::isGroupDefined)
                .forEach(this::addGroupedTab);
    }

    private void addGroupedTab(SettingTab settingTab) {
        String[] groupPath = settingTab.getGroup();
        MenuNode parent = null;
        List<String> currentPath = new ArrayList<>();

        for (String groupName : groupPath) {
            currentPath.add(groupName);
            List<String> pathKey = List.copyOf(currentPath);
            MenuNode groupNode = groupNodes.get(pathKey);

            if (groupNode == null) {
                if (parent == null) {
                    groupNode = rootNodes.get(groupName);
                }

                if (groupNode == null) {
                    groupNode = new MenuNode(groupName, null, pathKey);
                    if (parent == null) {
                        rootNodes.put(groupName, groupNode);
                        addRootItems(groupNode);
                    } else {
                        addItem(parent, groupNode);
                    }
                }

                groupNodes.put(pathKey, groupNode);
            }

            parent = groupNode;
        }

        String tabName = settingTab.getTitle();
        if (tabNodes.containsKey(tabName)) {
            return;
        }

        MenuNode tabNode = new MenuNode(tabName, settingTab);
        tabNodes.put(tabName, tabNode);
        addItem(parent, tabNode);
    }

    private static boolean isGroupDefined(SettingTab settingTab) {
        return settingTab.getGroup() != null && settingTab.getGroup().length > 0;
    }

    public void reload(List<SettingTab> settingTabs) {
        new ArrayList<>(getRootItems()).forEach(this::removeItem);
        groupNodes.clear();
        rootNodes.clear();
        tabNodes.clear();
        loadAll(settingTabs);
    }

    public Set<String> itemNames() {
        Set<String> names = new HashSet<>(tabNodes.keySet());
        groupNodes.keySet().forEach(path -> names.add(path.getLast()));
        return names;
    }

    MenuNode getNodeByName(String node) {
        MenuNode tabNode = tabNodes.get(node);
        if (tabNode != null) {
            return tabNode;
        }

        return groupNodes.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getKey().getLast(), node))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    boolean isVisible(MenuNode node, String query) {
        if (isVisibleNode(node, query)) return true;
        return getChildren(node).stream().anyMatch(child -> isVisible(child, query));
    }

    static boolean isVisibleNode(MenuNode node, String query) {
        boolean matches = node.name() != null && node.name().toLowerCase().contains(query);
        if (matches) return true;

        if (node.entity() != null) {
            String searchIndex = node.entity().getSearchIndex();
            if (searchIndex != null) {
                searchIndex = searchIndex.toLowerCase();
                matches = searchIndex.contains(query);
            }
        }
        return matches;
    }
}
