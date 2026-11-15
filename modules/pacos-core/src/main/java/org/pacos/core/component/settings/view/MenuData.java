package org.pacos.core.component.settings.view;

import com.vaadin.flow.data.provider.hierarchy.TreeData;
import org.pacos.base.component.setting.SettingTab;

import java.util.*;

/**
 * Builds and maintains the settings navigation tree.
 *
 * <p>Groups are identified by their complete path, so groups with the same name
 * can exist under different parents. Tab names are globally unique; the first
 * tab with a given name is added to the tree.</p>
 */
public class MenuData extends TreeData<MenuNode> {

    private final Map<List<String>, MenuNode> groupNodes = new HashMap<>();
    private final Map<String, MenuNode> tabNodes = new HashMap<>();

    public MenuData(List<SettingTab> settingTabs) {
        loadAll(settingTabs);
    }

    private void loadAll(List<SettingTab> settingTabs) {
        settingTabs.stream()
                .filter(tab -> !hasGroup(tab))
                .map(tab -> new MenuNode(tab.getTitle(), tab))
                .sorted(Comparator.comparingInt(MenuNode::getOrder).thenComparing(MenuNode::name))
                .forEach(this::addRootTab);

        settingTabs.stream()
                .filter(MenuData::hasGroup)
                .forEach(this::addGroupedTab);
    }

    private void addRootTab(MenuNode tabNode) {
        if (tabNodes.containsKey(tabNode.name())) {
            return;
        }

        tabNodes.put(tabNode.name(), tabNode);
        addRootItems(tabNode);
    }

    private void addGroupedTab(SettingTab settingTab) {
        String title = settingTab.getTitle();
        if (tabNodes.containsKey(title)) {
            return;
        }

        MenuNode parent = getOrCreateGroupPath(settingTab.getGroup());
        MenuNode tabNode = new MenuNode(title, settingTab);
        tabNodes.put(title, tabNode);
        addItem(parent, tabNode);
    }

    private MenuNode getOrCreateGroupPath(String[] groupPath) {
        MenuNode parent = null;
        List<String> currentPath = new ArrayList<>();

        for (String groupName : groupPath) {
            currentPath.add(groupName);
            List<String> pathKey = List.copyOf(currentPath);
            MenuNode groupNode = groupNodes.get(pathKey);

            if (groupNode == null) {
                groupNode = parent == null ? findRootNode(groupName) : null;

                if (groupNode == null) {
                    groupNode = new MenuNode(groupName, null, pathKey);
                    if (parent == null) {
                        addRootItems(groupNode);
                    } else {
                        addItem(parent, groupNode);
                    }
                }

                groupNodes.put(pathKey, groupNode);
            }

            parent = groupNode;
        }

        return parent;
    }

    private MenuNode findRootNode(String name) {
        return getRootItems().stream()
                .filter(node -> node.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    private static boolean hasGroup(SettingTab settingTab) {
        String[] group = settingTab.getGroup();
        return group != null && group.length > 0;
    }

    public void reload(List<SettingTab> settingTabs) {
        new ArrayList<>(getRootItems()).forEach(this::removeItem);
        groupNodes.clear();
        tabNodes.clear();
        loadAll(settingTabs);
    }

    public Set<String> itemNames() {
        Set<String> names = new HashSet<>(tabNodes.keySet());
        groupNodes.keySet().forEach(path -> names.add(path.getLast()));
        return names;
    }

    MenuNode getNodeByName(String name) {
        MenuNode tabNode = tabNodes.get(name);
        if (tabNode != null) {
            return tabNode;
        }

        return groupNodes.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getKey().getLast(), name))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    boolean isVisible(MenuNode node, String query) {
        if (isVisibleNode(node, query)) {
            return true;
        }
        return getChildren(node).stream().anyMatch(child -> isVisible(child, query));
    }

    static boolean isVisibleNode(MenuNode node, String query) {
        boolean matches = node.name() != null && node.name().toLowerCase().contains(query);
        if (matches) {
            return true;
        }

        if (node.entity() != null) {
            String searchIndex = node.entity().getSearchIndex();
            if (searchIndex != null) {
                return searchIndex.toLowerCase().contains(query);
            }
        }
        return false;
    }
}
