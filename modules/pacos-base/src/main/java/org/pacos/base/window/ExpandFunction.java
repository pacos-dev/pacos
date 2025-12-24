package org.pacos.base.window;

import org.pacos.base.utils.ObjectMapperUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

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
                                + "$0.$.overlay.shadowRoot.getElementById('overlay').style.getPropertyValue('top')]",
                        dw)
                .then(String.class, this::writeCurrentPosition);
    }

    void writeCurrentPosition(String jsonString) {
        try {
            ObjectMapper mapper = ObjectMapperUtils.getMapper();
            JsonNode e = mapper.readTree(jsonString);

            String left = e.get(0).asText();
            String top = e.get(1).asText();

            setLeft(left);
            setTop(top);

            dw.setPosition("0px", "0px");

        } catch (Exception ex) {
            throw new RuntimeException("Błąd parsowania pozycji JSON", ex);
        }
    }

    String getLeft() {
        return left;
    }

    String getTop() {
        return top;
    }
}