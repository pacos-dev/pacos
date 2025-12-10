package org.pacos.base.window;

import elemental.json.JsonArray;

/**
 * Stores the current position and configuration of the window so that it can be restored to its pre-extension state.
 * Modifies the window state on the frontend side by getting the window position before it was changed.
 */
public class ExpandFunction {

    private final DesktopWindow dw;
    private boolean expanded;
    private String width;
    private String height;
    private String left;
    private String top;

    ExpandFunction(boolean expanded, String width, String height, DesktopWindow desktopWindow) {
        this.expanded = expanded;
        this.width = width;
        this.height = height;
        this.dw = desktopWindow;
    }

    public boolean isExpanded() {
        return expanded;
    }

    void modify() {
        dw.setWidth(width);
        dw.setHeight(height);
        dw.setDraggable(!expanded);
        dw.setResizable(!expanded);
    }

    void expand() {
        if (expanded) {
            dw.setWidth(width);
            dw.setHeight(height);
            dw.setPosition(left, top);
        } else {
            readCurrentPosition();
            this.width = dw.getWidth();
            this.height = dw.getHeight();
            dw.setWidth("100%");
            dw.setHeight("100%");

        }
        dw.setDraggable(expanded);
        dw.setResizable(expanded);
        this.expanded = !expanded;
    }

    void setLeft(String left) {
        this.left = left;
    }

    void setTop(String top) {
        this.top = top;
    }

    void readCurrentPosition() {
        dw.getElement().executeJs(
                        "return [$0.$.overlay.shadowRoot.getElementById('overlay').style.getPropertyValue('left'),"
                                + "$0.$.overlay.shadowRoot.getElementById('overlay').style.getPropertyValue('top')]", dw)
                .then(JsonArray.class, this::writeCurrentPosition);
    }

    void writeCurrentPosition(JsonArray e) {
        setLeft(e.getString(0));
        setTop(e.getString(1));
        dw.setPosition("0px", "0px");
    }

    String getLeft() {
        return left;
    }

    String getTop() {
        return top;
    }
}
