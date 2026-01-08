package org.pacos.core.system.view;

import com.vaadin.flow.component.Component;
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
        UI.getCurrent().getPage().executeJs("window.systemClock($0)", Instant.now().toEpochMilli() + "");
    }

    public static void highlightText(Component element, String text){
        UI.getCurrent().getPage().executeJs("window.highlightSearchTerm($0,$1)",element.getElement(),text);
    }
}
