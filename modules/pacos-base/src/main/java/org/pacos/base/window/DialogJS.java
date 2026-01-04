package org.pacos.base.window;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;

import java.util.UUID;

/**
 * Allows to trigger js directly on client side for dialog
 */
public final class DialogJS {

    private DialogJS() {

    }

    /**
     * Set CSS style on overlay window
     * Insert param as a valid CSS configuration or 'unset'
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
                "window.bringToFront($0,$1)", dialog.getElement(), callback);
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

    public static void cleanUpWindowOnFront(Dialog dialog) {
        dialog.getElement().executeJs("window.cleanupWindowOnFront($0)", dialog);
    }
}
