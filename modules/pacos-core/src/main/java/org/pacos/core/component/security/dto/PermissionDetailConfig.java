package org.pacos.core.component.security.dto;

import org.pacos.base.security.Permission;
import org.pacos.base.session.AccessDecision;

public class PermissionDetailConfig {

    private final Permission action;
    private final AccessDecision decision;

    public PermissionDetailConfig(String key, String label, String category,String description,String decision) {
        this.action = new Permission() {

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
                return category;
            }

            @Override
            public String getDescription() {
                return description;
            }
        };
        this.decision = AccessDecision.valueOf(decision);
    }

    public PermissionDetailConfig(Permission action, AccessDecision decision) {
        this.action = action;
        this.decision = decision;
    }

    public String getKey() {
        return action.getKey();
    }

    public String getLabel() {
        return action.getLabel();
    }

    public String getCategory() {
        return action.getCategory();
    }

    public String getDescription() {
        return action.getDescription();
    }
    public AccessDecision getDecision() {
        return decision;
    }
}
