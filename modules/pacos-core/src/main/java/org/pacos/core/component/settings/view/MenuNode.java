package org.pacos.core.component.settings.view;

import org.pacos.base.component.setting.SettingTab;

/**
 * Represents a node in the settings navigation tree.
 *
 * <p>Menu nodes intentionally use object identity for equality. Group nodes can have
 * the same display name when they belong to different parent groups, for example
 * {@code System/General} and {@code Plugins/General}.</p>
 */
final class MenuNode {

    private final String name;
    private final SettingTab entity;

    MenuNode(String name, SettingTab entity) {
        this.name = name;
        this.entity = entity;
    }

    String name() {
        return name;
    }

    SettingTab entity() {
        return entity;
    }

    public int getOrder() {
        return entity == null ? 0 : entity.getOrder();
    }
}
