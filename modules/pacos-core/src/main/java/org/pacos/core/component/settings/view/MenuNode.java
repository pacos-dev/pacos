package org.pacos.core.component.settings.view;

import org.pacos.base.component.setting.SettingTab;

import java.util.List;

/**
 * Represents a node in the settings navigation tree.
 *
 * @param name display name of the menu node
 * @param entity settings tab represented by this node, or {@code null} for a group
 * @param groupPath full path identifying a group node
 */
record MenuNode(String name, SettingTab entity, List<String> groupPath) {

    MenuNode(String name, SettingTab entity) {
        this(name, entity, null);
    }

    public int getOrder() {
        if (entity == null) {
            return 0;
        }
        return entity.getOrder();
    }
}
