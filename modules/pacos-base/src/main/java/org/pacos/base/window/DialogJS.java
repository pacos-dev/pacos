package org.pacos.base.window;

import java.util.UUID;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;

/**
 * Allows to trigger js directly on client side for dialog
 */
public final class DialogJS {

    private DialogJS() {

    }

    public static void setPositionWithTimeout(String left, String top, Dialog dialog) {
        dialog.getUI().ifPresent(ui -> ui.getPage().executeJs(
                "$0.$.overlay.shadowRoot.getElementById('overlay').style.setProperty('left',$1); "
                        + "$0.$.overlay.shadowRoot.getElementById('overlay').style.setProperty('top',$2);",
                dialog, left, top));
    }


    public static void setPositionWithTimeout(String left, String top, Dialog dialog, int timeout) {
        String positionTimeoutJs = """
                        setTimeout(() => {
                           $0.$.overlay.shadowRoot.getElementById('overlay').style.setProperty('left',$1);
                           $0.$.overlay.shadowRoot.getElementById('overlay').style.setProperty('top',$2);
                        , $3);
                """;
        dialog.getUI().ifPresent(ui -> ui.getPage().executeJs(positionTimeoutJs,
                dialog, left, top, timeout));
    }

    /**
     * Set css style on overlay window
     * Insert param as a valid css configuration or 'unset'
     */
    public static void setAbsolutePosition(String left, String top, String right, Dialog dialog) {
        dialog.addAttachListener(e ->
                dialog.getUI().ifPresent(ui -> ui.getPage().executeJs(
                        "$0.$.overlay.shadowRoot.getElementById('overlay').style.setProperty('position','absolute');" +
                                "$0.$.overlay.shadowRoot.getElementById('overlay').style.setProperty('left',$1); "
                                + "$0.$.overlay.shadowRoot.getElementById('overlay').style.setProperty('top',$2); "
                                + "$0.$.overlay.shadowRoot.getElementById('overlay').style.setProperty('right',$3); ",
                        dialog, left, top, right)));
    }

    /**
     * Brings a given floating panel to the front.
     */
    public static void moveToFront(Dialog dialog) {
        String id = dialog.getId().orElse("dlg-" + UUID.randomUUID());
        if (dialog.getId().isEmpty()) {
            dialog.setId(id);
        }
        boolean callback = dialog instanceof DesktopWindow;
        UI.getCurrent().getPage().executeJs(
                "window.bringToFront($0,$1)", id, callback);
    }

    public static void enableResizingOnHeaderDblClick(Dialog dialog) {
        String resizeOnDoubleClickJS = """
                requestAnimationFrame(() => {
                          const overlay = this.$.overlay;
                          if (overlay) {
                
                            const resizer = overlay.shadowRoot.querySelector('section[id="resizerContainer"]');
                            const header = overlay.shadowRoot.querySelector('header[part="header"]');
                
                            if (resizer) {
                              resizer.addEventListener('dblclick', (event) => {
                                    if (event.target == resizer) {
                                      $0.$server.onHeaderDoubleClick();
                                      event.stopImmediatePropagation();
                                    }
                              });
                            }
                          }
                        });
                """;

        dialog.addAttachListener(e ->
                dialog.getElement().executeJs(resizeOnDoubleClickJS, dialog
                ));
    }

    public static void monitorWindowOnFront(Dialog dialog) {
        dialog.getElement().executeJs("window.monitorWindowOnFront($0)", dialog);
    }

    /**
     * When a dialog is added, it receives a z-index based on the number of windows added in the DOM. Unfortunately,
     * the z-index is incremented each time the user changes the active window. Therefore, for two windows after
     * changes, the maximum z-index can be 210, and a newly added modal receives a z-index of 202. To avoid this, a
     * newly added modal receives an overloaded z-index
     */
    public static void moveToAbsoluteTop(Dialog dialog, int zIndex) {
        dialog.getUI().ifPresent(ui -> ui.getPage().executeJs(
                "$0.$.overlay.style.setProperty('z-index','" + zIndex + "');",
                dialog));
    }
}
