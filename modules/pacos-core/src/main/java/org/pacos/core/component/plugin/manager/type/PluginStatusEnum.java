package org.pacos.core.component.plugin.manager.type;

public enum PluginStatusEnum {

    OFF, INITIALIZATION, ERROR, ON, SHUTDOWN;

    public boolean canRun() {
        return this == ERROR || this == OFF;
    }

    public boolean canStop() {
        return this == ON || this == INITIALIZATION || this == SHUTDOWN;
    }

    public boolean canProcess() {
        return this == ON || this == OFF || this == ERROR;
    }

    public boolean isInitialized() {
        return this == INITIALIZATION;
    }

    public boolean isOn() {
        return this == ON;
    }
}
