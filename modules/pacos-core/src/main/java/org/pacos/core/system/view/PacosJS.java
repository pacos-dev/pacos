package org.pacos.core.system.view;

import com.vaadin.flow.component.UI;

import java.time.Instant;

public final class PacosJS {

    private PacosJS() {
    }

    static void initializeScripts() {
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

    private static void enableDragAndDropForDockElem() {
        UI.getCurrent().getPage().executeJs("window.dockDraggable()");
    }

    private static void runSystemClock() {
        UI.getCurrent().getPage().executeJs("window.systemClock()", Instant.now().toEpochMilli() + "");
    }
}
