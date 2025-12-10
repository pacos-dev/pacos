package org.pacos.core.component.installer.settings;

public enum InstallationMode {
    SINGLE, MULTI;

    public boolean isSingle() {
        return this.equals(SINGLE);
    }

    public boolean isMulti() {
        return this.equals(MULTI);
    }
}
