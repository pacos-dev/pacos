package org.pacos.core.component.variable;

import lombok.Getter;
import org.pacos.base.security.Permission;
import org.pacos.core.component.security.PermissionCategory;

@Getter
public enum VariablePermissions implements Permission {

    EDIT_GLOBAL_VARIABLE("variable.global.edit", "Edit global variables",
            PermissionCategory.VARIABLE.getName(),
            "Allow to add/remove/edit global variables");

    private final String key;
    private final String label;
    private final String category;
    private final String description;

    VariablePermissions(String key, String label, String category, String description) {
        this.key = key;
        this.label = label;
        this.category = category;
        this.description = description;
    }

}
