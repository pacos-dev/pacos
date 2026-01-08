package org.pacos.core.component.settings.view;

import org.junit.jupiter.api.Test;
import org.pacos.base.component.setting.SettingTab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MenuNodeTest {

    @Test
    void whenEntityIsNullThenOrderReturnsZero() {
        //given
        MenuNode node = new MenuNode("Folder", null);

        //when
        int order = node.getOrder();

        //then
        assertEquals(0, order);
    }

    @Test
    void whenEntityIsPresentThenOrderReturnsValueFromEntity() {
        //given
        SettingTab tab = mock(SettingTab.class);
        when(tab.getOrder()).thenReturn(150);
        MenuNode node = new MenuNode("Settings", tab);

        //when
        int order = node.getOrder();

        //then
        assertEquals(150, order);
    }

    @Test
    void whenRecordIsCreatedThenPropertiesAreStoredCorrectly() {
        //given
        String expectedName = "Test Node";
        SettingTab expectedEntity = mock(SettingTab.class);

        //when
        MenuNode node = new MenuNode(expectedName, expectedEntity);

        //then
        assertEquals(expectedName, node.name());
        assertEquals(expectedEntity, node.entity());
    }
}