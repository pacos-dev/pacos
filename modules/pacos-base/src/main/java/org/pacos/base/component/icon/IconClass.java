package org.pacos.base.component.icon;

public enum IconClass {
    RED_CIRCLE("red-circle");

    private final String name;

    IconClass(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
