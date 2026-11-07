package org.pacos.core.component.credential.security;

import org.pacos.base.security.Permission;

public enum CredentialPermission implements Permission {

    VIEW("credential.view", "View credentials", "View credential metadata"),
    CREATE("credential.create", "Create credentials", "Create new credentials"),
    UPDATE("credential.update", "Update credentials", "Update owned credentials"),
    DELETE("credential.delete", "Delete credentials", "Delete owned credentials"),
    USE("credential.use", "Use credentials", "Resolve credential values"),
    MANAGE_ACCESS("credential.manage.access", "Manage credential access", "Share credentials with users and plugins");

    private final String key;
    private final String label;
    private final String description;

    CredentialPermission(String key, String label, String description) {
        this.key = key;
        this.label = label;
        this.description = description;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getCategory() {
        return "system";
    }

    @Override
    public String getDescription() {
        return description;
    }
}
