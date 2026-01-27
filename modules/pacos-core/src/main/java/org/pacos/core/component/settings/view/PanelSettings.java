package org.pacos.core.component.settings.view;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.pacos.base.component.NoContent;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.component.SplitterUtils;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.plugin.manager.PluginResource;
import org.pacos.core.component.settings.view.config.SettingsConfig;
import org.pacos.core.system.view.PacosJS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;

@Component
@Scope("prototype")
public class PanelSettings extends DesktopWindow {

    private static final Logger LOG = LoggerFactory.getLogger(PanelSettings.class);
    private final Map<MenuNode, SettingPageLayout> contentMap = new HashMap<>();
    private final UserSession userSession;
    private final Scroller content = new Scroller();

    private final MenuGrid menuGrid;

    public PanelSettings(SettingsConfig moduleConfig) {
        super(moduleConfig);
        setSize(900, 500);
        this.userSession = UserSession.getCurrent();
        this.menuGrid = new MenuGrid(loadAllowedSettingTabs());

        add(new SplitterUtils(menuGrid, content, 25).orientation(SplitLayout.Orientation.HORIZONTAL));
        menuGrid.addSelectionListener(e -> menuItemSelectListener(e.getFirstSelectedItem()));
        menuGrid.selectFirstItem();

        super.registerShortcut(ShortcutType.SAVE, e -> triggerShortcutOnSelectedTab(ShortcutType.SAVE));
        super.registerShortcut(ShortcutType.DELETE, e -> triggerShortcutOnSelectedTab(ShortcutType.DELETE));

        UISystem.getCurrent().subscribeOnAttached(this, ModuleEvent.PLUGIN_UNINSTALLED, plugin -> removeTabOnPluginUninstalled());
        UISystem.getCurrent().subscribeOnAttached(this, ModuleEvent.PLUGIN_INSTALLED, plugin -> addTabWhenPluginInstalled());
    }

    private void triggerShortcutOnSelectedTab(ShortcutType shortcut) {
        menuGrid.getSelectionModel().getFirstSelectedItem().ifPresent(node ->
                contentMap.get(node).onShortCutDetected(shortcut));
    }

    private void menuItemSelectListener(Optional<MenuNode> menuNodeOpt) {
        if (menuNodeOpt.isEmpty()) {
            NoContent noContent = new NoContent("No result was found", "");
            content.setContent(noContent);
        } else {
            showContentForSelectedItem(menuNodeOpt.get());
        }
    }

    private void showContentForSelectedItem(MenuNode menuNode) {
        try {
            SettingTab settingTab = menuNode.entity();
            if (settingTab == null) {
                NoContent noContent = new NoContent("No definition for this tab was found", "");
                content.setContent(noContent);
            } else {
                SettingPageLayout tabLayout = contentMap.computeIfAbsent(menuNode, node -> node.entity().generateContent());
                tabLayout.setSizeFull();
                content.setContent(tabLayout);
                PacosJS.highlightText(content.getContent(), menuGrid.getSearchQuery());
            }
        } catch (Exception ex) {
            NotificationUtils.error(ex);
            LOG.error("Can't load setting page layout: ", ex);
        }
    }

    private List<SettingTab> loadAllowedSettingTabs() {
        Set<SettingTab> settingsTabSet = PluginResource.loadAvailableSettingTabs(userSession);
        return settingsTabSet.stream()
                .sorted(Comparator.comparing(SettingTab::getOrder))
                .sorted(Comparator.comparing(SettingTab::getTitle))
                .toList();
    }

    private void removeTabOnPluginUninstalled() {
        if (this.getUi() != null && this.getUi().getSession() != null) {
            this.getUi().getSession().access(this::removeTab);
        }
    }

    protected void removeTab() {
        Set<String> existingKeys = loadAllowedSettingTabs().stream().map(SettingTab::getTitle).collect(Collectors.toSet());
        Set<String> keys = new HashSet<>(menuGrid.itemNames());
        existingKeys.removeAll(keys);

        existingKeys.forEach(node -> {
            MenuNode nodeItem = menuGrid.getNodeByName(node);
            contentMap.remove(nodeItem);
            if (menuGrid.getSelectedItems().contains(nodeItem)) {
                menuGrid.selectFirstItem();
            }
        });
        reloadSettingMenu();
    }

    void reloadSettingMenu() {
        menuGrid.reload(loadAllowedSettingTabs());
        menuGrid.getDataProvider().refreshAll();
    }

    protected void addTabWhenPluginInstalled() {
        if (this.getUi() != null && this.getUi().getSession() != null) {
            this.getUi().getSession().access(this::reloadSettingMenu);
        }
    }

    TreeGrid<MenuNode> getMenuGrid() {
        return menuGrid;
    }
}
