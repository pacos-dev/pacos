package org.pacos.core.component.settings.view;

import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import org.pacos.base.component.Theme;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope("prototype")
public class PanelSettings extends DesktopWindow {

    private static final Logger LOG = LoggerFactory.getLogger(PanelSettings.class);

    protected final Map<Tab, SettingTab> tabMap = new HashMap<>();
    private final Map<SettingTab, SettingPageLayout> contentMap = new HashMap<>();
    private final Tabs tabs = new Tabs();
    private final UserSession userSession;

    public PanelSettings(SettingsConfig moduleConfig) {
        super(moduleConfig);
        this.userSession = UserSession.getCurrent();
        configureMenu();

        setSize(900, 500);

        tabs.addThemeVariants(TabsVariant.LUMO_SMALL);
        tabs.addThemeName(Theme.NO_MARGIN.getName());
        tabs.addThemeName(Theme.NO_PADDING.getName());
        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        Scroller content = new Scroller();
        add(new SplitterUtils(tabs, content, 25).orientation(SplitLayout.Orientation.HORIZONTAL));

        tabs.addSelectedChangeListener(e -> tabSelectionChangeEvent(e.getSelectedTab(), content));
        if (!tabMap.isEmpty()) {
            content.setContent(tabMap.get(tabs.getSelectedTab()).generateContent());
        }

        super.registerShortcut(ShortcutType.SAVE,
                e -> contentMap.get(tabMap.get(tabs.getSelectedTab())).onShortCutDetected(ShortcutType.SAVE));
        super.registerShortcut(ShortcutType.DELETE,
                e -> contentMap.get(tabMap.get(tabs.getSelectedTab())).onShortCutDetected(ShortcutType.DELETE));

        UISystem.getCurrent().subscribeOnAttached(this, ModuleEvent.PLUGIN_UNINSTALLED, plugin -> removeTabOnPluginUninstalled());
        UISystem.getCurrent().subscribeOnAttached(this, ModuleEvent.PLUGIN_INSTALLED, plugin -> addTabWhenPluginInstalled());
    }

    private void tabSelectionChangeEvent(Tab e, Scroller content) {
        try {
            SettingTab settingTab = tabMap.get(e);
            SettingPageLayout tabLayout = contentMap.computeIfAbsent(settingTab, SettingTab::generateContent);
            content.setContent(tabLayout);
        } catch (Exception ex) {
            NotificationUtils.error(ex);
            LOG.error("Can't load setting page layout: ", ex);
        }
    }

    private void configureMenu() {
        List<SettingTab> settingsTab = loadAllowedSettingTabs();
        settingsTab.forEach(r -> {
            final Tab tab = new Tab(r.getTitle());
            tabs.add(tab);
            tabMap.put(tab, r);
        });
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
        List<SettingTab> settingsTab = loadAllowedSettingTabs();
        List<Tab> tabsToRemove = tabMap.entrySet().stream()
                .filter(e -> !settingsTab.contains(e.getValue()))
                .map(Map.Entry::getKey).toList();

        tabsToRemove.forEach(tab -> {
            tabMap.remove(tab);
            if (tabs.getSelectedTab().equals(tab)) {
                tabs.setSelectedTab(tabs.getTabAt(0));
            }
            tabs.remove(tab);
        });
    }

    protected void addTabWhenPluginInstalled() {
        if (this.getUi() != null && this.getUi().getSession() != null) {
            this.getUi().getSession().access(this::addTab);
        }
    }

    protected void addTab() {
        List<SettingTab> settingsTab = loadAllowedSettingTabs();
        for (SettingTab tab : settingsTab) {
            if (!tabMap.containsValue(tab)) {
                final Tab newTab = new Tab(tab.getTitle());
                tabs.add(newTab);
                tabMap.put(newTab, tab);
            }
        }
    }

}
