package org.pacos.common.data;

import org.pacos.base.component.Color;

public enum RequestMethodType {

    GET(Color.GREEN),
    POST(Color.ORANGE),
    PUT(Color.LIGHTBLUE),
    DELETE(Color.RED),
    PATCH(Color.ORANGE),
    HEAD(Color.YELLOW),
    OPTIONS(Color.BLACK_LITGHT),
    TRACE(Color.TEXT);

    private final Color color;

    RequestMethodType(Color color) {
        this.color = color;
    }

    public static RequestMethodType byName(String method) {
        for (RequestMethodType type : RequestMethodType.values()) {
            if (type.name().equalsIgnoreCase(method)) {
                return type;
            }
        }
        return RequestMethodType.GET;
    }

    public Color getColor() {
        return color;
    }
}
