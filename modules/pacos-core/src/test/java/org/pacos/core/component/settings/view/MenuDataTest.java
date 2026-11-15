package org.pacos.core.component.settings.view;

import org.junit.jupiter.api.Test;
import org.pacos.base.component.setting.SettingTab;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MenuDataTest {

    @Test
    void whenReloadCalledThenOldNodesAreRemoved() {
        MenuData menuData = new MenuData(List.of(settingTab("Old", "System", "Security")));

        menuData.reload(List.of(settingTab("New", "System", "Security")));

        assertNull(menuData.getNodeByName("Old"));
        assertNotNull(menuData.getNodeByName("New"));
        assertEquals(1, menuData.getRootItems().size());
        MenuNode system = menuData.getRootItems().getFirst();
        MenuNode security = menuData.getChildren(system).getFirst();
        assertEquals("New", menuData.getChildren(security).getFirst().name());
    }

    @Test
    void whenSettingsTabHasNestedGroupsThenEachGroupIsAddedToItsParent() {
        MenuData menuData = new MenuData(List.of(settingTab("Users", "System", "Security", "Users")));

        MenuNode system = menuData.getRootItems().getFirst();
        MenuNode security = menuData.getChildren(system).getFirst();
        MenuNode usersGroup = menuData.getChildren(security).getFirst();
        MenuNode usersTab = menuData.getChildren(usersGroup).getFirst();

        assertEquals("System", system.name());
        assertEquals("Security", security.name());
        assertEquals("Users", usersGroup.name());
        assertNull(usersGroup.entity());
        assertEquals("Users", usersTab.name());
        assertNotNull(usersTab.entity());
    }

    @Test
    void whenDifferentGroupPathsShareSegmentNameThenSeparateNodesAreCreated() {
        MenuData menuData = new MenuData(List.of(
                settingTab("First", "System", "General"),
                settingTab("Second", "Plugins", "General")
        ));

        MenuNode system = menuData.getRootItems().stream()
                .filter(node -> node.name().equals("System"))
                .findFirst()
                .orElseThrow();
        MenuNode plugins = menuData.getRootItems().stream()
                .filter(node -> node.name().equals("Plugins"))
                .findFirst()
                .orElseThrow();
        MenuNode systemGeneral = menuData.getChildren(system).getFirst();
        MenuNode pluginsGeneral = menuData.getChildren(plugins).getFirst();

        assertNotSame(systemGeneral, pluginsGeneral);
        assertEquals("General", systemGeneral.name());
        assertEquals("General", pluginsGeneral.name());
        assertEquals("First", menuData.getChildren(systemGeneral).getFirst().name());
        assertEquals("Second", menuData.getChildren(pluginsGeneral).getFirst().name());
    }

    @Test
    void whenReloadCalledRepeatedlyThenNewParentReferencesAreUsed() {
        MenuData menuData = new MenuData(List.of(settingTab("Initial", "System", "Security")));

        menuData.reload(List.of(settingTab("Updated", "System", "Security")));
        menuData.reload(List.of(settingTab("Final", "System", "Security")));

        MenuNode system = menuData.getRootItems().getFirst();
        MenuNode security = menuData.getChildren(system).getFirst();

        assertEquals(1, menuData.getRootItems().size());
        assertEquals(1, menuData.getChildren(system).size());
        assertEquals("Final", menuData.getChildren(security).getFirst().name());
        assertTrue(menuData.itemNames().contains("Final"));
        assertFalse(menuData.itemNames().contains("Initial"));
        assertFalse(menuData.itemNames().contains("Updated"));
    }

    private SettingTab settingTab(String title, String... group) {
        SettingTab settingTab = mock(SettingTab.class);
        when(settingTab.getTitle()).thenReturn(title);
        when(settingTab.getGroup()).thenReturn(group);
        return settingTab;
    }
}
