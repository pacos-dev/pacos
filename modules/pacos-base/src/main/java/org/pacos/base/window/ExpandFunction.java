package org.pacos.base.window;

import org.pacos.base.exception.PacosException;

import java.util.List;

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


    void readCurrentPosition() {
        dw.getElement().executeJs(
                        "return [$0.$.overlay.shadowRoot.getElementById('overlay').style.getPropertyValue('left'),"
                                + "$0.$.overlay.shadowRoot.getElementById('overlay').style.getPropertyValue('top')]",
                        dw)
                .then(List.class, this::writeCurrentPosition);
    }

    void writeCurrentPosition(List<?> jsonList) {
        try {

            this.left = jsonList.get(0).toString();
            this.top = jsonList.get(1).toString();
            dw.setPosition("0px", "0px");

        } catch (Exception ex) {
            throw new PacosException("Error while parsing JSON", ex);
        }
    }

    String getLeft() {
        return left;
    }

    String getTop() {
        return top;
    }
}