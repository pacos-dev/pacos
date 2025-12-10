package org.pacos.base.session;

public enum AccessDecision {
    ALLOW, DENY;

    public boolean isAllowed() {
        return this == ALLOW;
    }
}