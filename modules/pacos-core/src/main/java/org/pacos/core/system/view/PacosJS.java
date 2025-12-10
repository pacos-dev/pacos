package org.pacos.core.system.view;

import java.time.Instant;

import com.vaadin.flow.component.UI;

public final class PacosJS {

    private PacosJS() {
    }

    static void initializeScripts() {
        showOrHideDockOnMouseOver();
        enableDragAndDropForDockElem();
        runSystemClock();
    }

    public static void pasteToClipboard(String value) {
        UI.getCurrent().getPage().executeJs("""
                          navigator.clipboard.writeText($0)
                        .catch(err => console.error('Error writing text to clipboard', err));
                        """, value
        );
    }

    private static void showOrHideDockOnMouseOver() {
        UI.getCurrent().getPage().executeJs("document.addEventListener('mousemove', function(event){ "
                + "  var divRect = document.getElementById('desk').getBoundingClientRect(); "
                + "  var divRect2 = document.getElementById('osx-dock').getBoundingClientRect(); "
                + "  if (event.clientX >= divRect.left && event.clientX <= divRect.right && "
                + "      event.clientY >= divRect2.top && event.clientY <= divRect2.bottom) { "
                + "      document.getElementById('dockContainer').style.zIndex=100000; "
                + "    }else{ "
                + "   document.getElementById('dockContainer').style.zIndex=1; "
                + " } "
                + "}, false);");
    }

    private static void enableDragAndDropForDockElem() {
        UI.getCurrent().getPage().executeJs("window.dockDraggable()");
    }

    private static void runSystemClock() {
        UI.getCurrent().getPage().executeJs("window.systemClock()", Instant.now().toEpochMilli() + "");
    }
}
