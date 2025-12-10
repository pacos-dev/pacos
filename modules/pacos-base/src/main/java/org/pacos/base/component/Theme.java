package org.pacos.base.component;

public enum Theme {
    BLACK_WINDOW("black"),
    PARAM_GRID("param-g"),
    VALUE_GRID("value-g"),
    APP_STYLE("app-style"),
    APP_TREE("app-tree"),
    NO_BORDER("no-border"),
    SMALL("small"),
    REST("rest"),
    DOCK("dock"),
    LEFT_BORDER("left-border"),
    NO_PADDING("no-padding"),
    NO_MARGIN("no-margin"),
    HOME("home"),
    RIGHT_BORDER("right-border"),
    NO_SELECTABLE("no-selectable"),
    TAB_CLOSE("tab-close"),
    NO_RADIUS("no-radius"),
    FULL_SIZE("full-size");

    public final String themeName;

    Theme(String themeName) {
        this.themeName = themeName;
    }

    public String getName() {
        return themeName;
    }
}
