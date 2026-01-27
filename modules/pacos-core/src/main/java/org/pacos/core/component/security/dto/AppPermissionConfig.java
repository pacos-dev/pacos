package org.pacos.core.component.security.dto;

public class AppPermissionConfig {

    private final String key;
    private final String label;
    private final String category;
    private final String description;
    private final boolean active;
    private final Integer id;

    public AppPermissionConfig(Integer id, String key, String label, String category, String description, boolean active) {
        this.id = id;
        this.key = key;
        this.label = label;
        this.category = category;
        this.description = description;
        this.active = active;
    }

    public Integer getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }
}
