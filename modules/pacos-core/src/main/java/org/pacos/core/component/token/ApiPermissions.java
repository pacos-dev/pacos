package org.pacos.core.component.token;

import lombok.Getter;
import org.pacos.base.security.Permission;
import org.pacos.core.component.security.PermissionCategory;

@Getter
public enum ApiPermissions implements Permission {
    GENERATE_TOKEN("system.api.token.generate",
            "Generate token",
            PermissionCategory.API.getName(),
            "Allow to generate new API access token"),
    REMOVE_TOKEN("system.api.token.remove",
            "Remove token",
            PermissionCategory.API.getName(),
            "Allow to remove API access token"),
    REVOKE_TOKEN("system.api.token.revoke",
            "Revoke token",
            PermissionCategory.API.getName(),
            "Allow to revoke API access token"),
    ;

    private final String key;
    private final String label;
    private final String category;
    private final String description;

    ApiPermissions(String key, String label, String category, String description) {
        this.key = key;
        this.label = label;
        this.category = category;
        this.description = description;
    }

}
