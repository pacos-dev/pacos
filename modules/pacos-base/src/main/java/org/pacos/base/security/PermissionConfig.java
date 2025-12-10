package org.pacos.base.security;

import java.io.Serializable;

import org.pacos.base.session.AccessDecision;

public record PermissionConfig(String key, String accessDecision) implements Serializable {

    public AccessDecision decision() {
        return AccessDecision.valueOf(accessDecision);
    }
}
