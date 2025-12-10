package org.pacos.core.component.token.domain;

public enum ApiTokenStatus {
    ACTIVE, EXPIRED, REVOKED;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isExpired() {
        return this == EXPIRED;
    }

    public boolean isRevoked() {
        return this == REVOKED;
    }
}
