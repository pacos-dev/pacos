package org.pacos.base.component;

public enum Style {
    MARGIN_TOP("margin-top"),
    MARGIN_LEFT("margin-left"),
    USER_SELECT("user-select");
    private final String value;

    Style(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
