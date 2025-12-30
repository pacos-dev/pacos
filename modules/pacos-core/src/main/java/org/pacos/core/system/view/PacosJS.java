package org.pacos.core.system.view;

import com.vaadin.flow.component.UI;

import java.time.Instant;

public final class PacosJS {

    private PacosJS() {
    }

    static void initializeScripts() {
        moveDockToOverlay();
//        showOrHideDockOnMouseOver();
        enableDragAndDropForDockElem();
        runSystemClock();
    }

    private static void moveDockToOverlay() {
        UI.getCurrent().getPage().executeJs("window.attachDockToOverlay()");
    }

    public static void pasteToClipboard(String value) {
        UI.getCurrent().getPage().executeJs("""
                          navigator.clipboard.writeText($0)
                        .catch(err => console.error('Error writing text to clipboard', err));
                        """, value
        );
    }

    private static void showOrHideDockOnMouseOver() {
        UI.getCurrent().getPage().executeJs("""
                document.addEventListener('mousemove', function (event) {
                    const desk = document.getElementById('desk');
                    const dock = document.getElementById('dockContainer');
                    console.log('mouseover');
                    if (!desk || !dock) return;
             
                    const deskRect = desk.getBoundingClientRect();
                    const dockRect = dock.getBoundingClientRect();
                
                    if (
                        event.clientX >= deskRect.left &&
                        event.clientX <= deskRect.right &&
                        event.clientY >= dockRect.top &&
                        event.clientY <= dockRect.bottom
                    ) {
                        dock.style.zIndex = 100000;
                    } else {
                        dock.style.zIndex = 1;
                    }
                }, false);
              """
            );
    }

    private static void enableDragAndDropForDockElem() {
        UI.getCurrent().getPage().executeJs("window.dockDraggable()");
    }

    private static void runSystemClock() {
        UI.getCurrent().getPage().executeJs("window.systemClock()", Instant.now().toEpochMilli() + "");
    }
}
