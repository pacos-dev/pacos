package org.pacos.core.system.event;

import com.vaadin.flow.component.UI;
import org.pacos.base.window.DesktopWindow;

public final class OpenWindowEvent {

    private OpenWindowEvent() {

    }

    public static void fireEvent(DesktopWindow dw, UI ui) {
        dw.getElement().removeFromTree();
        ui.add(dw);
        dw.open();
        dw.moveToFront();
        dw.restorePosition();
    }

}
